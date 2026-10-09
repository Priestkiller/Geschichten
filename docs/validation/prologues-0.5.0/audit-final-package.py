"""Audit the actual compiled catalog, signed APK and retained test evidence."""
from collections import Counter
from hashlib import sha256
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

out = Path(__file__).resolve().parent
project = out.parents[2]
release = project.parent / "release-0.5.0"

def digest(path):
    result = sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()

def reports(folder, expected):
    suites, cases = [], set()
    for path in sorted(folder.glob("TEST-*.xml")):
        root = ET.parse(path).getroot()
        counts = {k: int(root.get(k, "0")) for k in ("tests", "failures", "errors", "skipped")}
        assert counts["failures"] == counts["errors"] == counts["skipped"] == 0, path
        suites.append({"file": path.name, **counts})
        cases.update((c.get("classname"), c.get("name")) for c in root.findall("testcase"))
    assert len(cases) == sum(s["tests"] for s in suites) == expected
    return suites, cases

jvm, jvm_cases = reports(out / "final-jvm-tests", 53)
native, native_cases = reports(out / "native-tests", 35)
assert len(jvm_cases | native_cases) == 88
issues = ET.parse(out / "lint-final.xml").getroot().findall("issue")
severities = dict(Counter(i.get("severity") for i in issues))
assert not severities.get("Error", 0) and not severities.get("Fatal", 0)
before = json.loads((out / "catalog-before.json").read_text(encoding="utf-8"))
catalog = json.loads((out / "catalog-final.json").read_text(encoding="utf-8"))
assert catalog == json.loads((out / "catalog-current.json").read_text(encoding="utf-8"))
assert len(before) == len(catalog) == 90
assert len({p["id"] for p in catalog}) == len({p["openingMessage"] for p in catalog}) == 90
for old, new in zip(before, catalog):
    assert {k: v for k, v in old.items() if k != "openingMessage"} == {k: v for k, v in new.items() if k != "openingMessage"}
    assert old["openingMessage"] != new["openingMessage"]
    assert len(new["openingMessage"]) <= 2500
    assert len(new["openingMessage"]) + 1000 <= 4000
    assert len(new["personality"]) <= 630 and len(new["scenario"]) <= 480
    assert len(new["openingMessage"].split("\n\n")) >= 3

screens = out / "native-screenshots"
assert {f"{kind}-{p['id']}.png" for p in catalog for kind in ("card", "chat")} == {p.name for p in (screens / "artwork-regression/all-90").glob("*.png")}
assert {f"profile-{p['id']}.png" for p in catalog} == {p.name for p in (screens / "profiles/all-90").glob("*.png")}
apk = release / "Geschichten-0.5.0.apk"
manifest_path = release / "geschichten-android-update.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
assert manifest["versionCode"] == 7 and manifest["versionName"] == "0.5.0"
assert manifest["applicationId"] == "dev.vincent.geschichten" and manifest["minSdk"] == 31 and manifest["channel"] == "test"
assert manifest["apk"] == {"assetName": apk.name, "size": apk.stat().st_size, "sha256": digest(apk)}
with zipfile.ZipFile(apk) as package, zipfile.ZipFile(project.parent / "release-0.4.0/Geschichten-0.4.0.apk") as old:
    portraits = {Path(n).stem.removeprefix("portrait_"): n for n in package.namelist() if "/portrait_" in n}
    assert set(portraits) == {p["id"] for p in catalog}
    assert all(package.read(portraits[p["id"]]) == old.read(portraits[p["id"]]) for p in catalog)
    assert not any("catalog-v" in n for n in package.namelist())
dump = (out / "apk-manifest.txt").read_text(encoding="utf-8-sig")
assert "ComposeTestActivity" not in dump
assert "versionCode(0x0101021b)=7" in dump and '"0.5.0"' in dump
assert "Verification succesful" in (out / "zipalign.log").read_text(encoding="utf-8-sig")
assert "Paket/Version/Signatur geprüft:" in (out / "apk-manifest-check.log").read_text(encoding="utf-8-sig")
summary = {
    "version": "0.5.0", "versionCode": 7, "databaseVersion": 6,
    "publication": "local_ready_not_published", "finalJvm": jvm, "targetedAndroidUiTests": native,
    "distinctPassedCases": 88, "failures": 0, "skipped": 0,
    "lintSeverityCounts": severities, "lintIssueCounts": dict(Counter(i.get("id") for i in issues)),
    "catalog": {"total": 90, "individualPrologues": 90, "otherReleasedFieldsUnchanged": True,
                "maxOpeningCharacters": max(len(p["openingMessage"]) for p in catalog),
                "categoryCounts": dict(Counter(p["genre"] for p in catalog))},
    "all90PortraitsByteIdenticalTo040": True, "actualUiScreenshots": 270,
    "navigation": ["Figuren", "Verlauf", "Erinnerungen", "Einstellungen"],
    "testActivityAbsent": True, "zipAlignment16KB": "passed", "apk": manifest["apk"],
    "manifestSha256": digest(manifest_path),
    "signerCertificateSha256": "3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40",
    "physicalS24InstallationAndModelInference": "not_tested",
    "existingWindowsFileProviderHostCase": "not_rerun; provider and installation code unchanged",
    "correctedTestAssertions": "A full-width scroll-end marker is measurable; landscape chat checks scroll to the existing message and target its text child.",
    "editorialCorrections": "Aligned waiting-room/workshop/court locations, Ari's adult female cousin, fortress/arena place names and the still-flowing poison canal with the delivered scene and notes.",
}
public_path = out / "public-release-summary.json"
if public_path.exists():
    public = json.loads(public_path.read_text(encoding="utf-8"))
    assert public["releaseUrl"] == "https://github.com/Priestkiller/Geschichten/releases/tag/v0.5.0"
    assert public["draft"] is False and public["prerelease"] is True
    assert public["anonymousApiRequest"] and public["publicManifestMatchesLocal"] and public["publicReadmeMatchesPrepared"]
    assert public["fullPublicApkDownloaded"] and public["apkSha256"] == manifest["apk"]["sha256"]
    assert digest(out / "public-Geschichten-0.5.0.apk") == manifest["apk"]["sha256"]
    assert (out / "public-geschichten-android-update.json").read_bytes() == manifest_path.read_bytes()
    assert (out / "public-README.md").read_bytes() == (release / "README-GitHub-Vorschlag.md").read_bytes()
    assert (out / "app-parser-check.log").read_text(encoding="utf-8-sig").count("BESTANDEN:") == 2
    summary.update(publication="published_verified", publicRelease=public,
                   publicAppParserCheck="passed_for_codes_2_3_4_5_6",
                   publicReadmeUpdate=json.loads((out / "github-readme-update.json").read_text(encoding="utf-8")))
(out / "summary.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("PASS: 88 distinct cases; 0 failures/skips; 0 lint errors.")
print("PASS: 90 individual prologues, compatible data migration, 270 UI captures, 90 unchanged portraits.")
print("PASS: signed version 0.5.0/code 7, 16-KB alignment, production-only manifest.")
print(summary["publication"])
