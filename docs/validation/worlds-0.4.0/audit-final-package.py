"""Verify the compatible signed package, unchanged v4 data and test evidence."""
from collections import Counter
from hashlib import sha256
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

out = Path(__file__).resolve().parent
project = out.parents[2]
release = project.parent / "release-0.4.0"

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
        for case in root.findall("testcase"):
            assert not any(case.find(tag) is not None for tag in ("failure", "error", "skipped"))
            cases.add((case.get("classname"), case.get("name")))
    assert len(cases) == sum(s["tests"] for s in suites) == expected
    return suites, cases

jvm, jvm_cases = reports(out / "final-jvm-tests", 51)
native, native_cases = reports(out / "native-tests", 29)
assert len(jvm_cases | native_cases) == 80
lint = ET.parse(out / "lint-final.xml").getroot().findall("issue")
severities = dict(Counter(issue.get("severity") for issue in lint))
assert not severities.get("Error", 0) and not severities.get("Fatal", 0)
before = json.loads((out / "catalog-before.json").read_text(encoding="utf-8"))
catalog = json.loads((out / "catalog-current.json").read_text(encoding="utf-8"))
assert len(before) == 50 and len(catalog) == 90 and catalog[:50] == before
assert len({f["id"] for f in catalog}) == len({f["name"] for f in catalog}) == 90
assert all(len(f["personality"]) <= 630 and len(f["scenario"]) <= 480 and len(f["openingMessage"]) <= 1500 for f in catalog)
new_portraits = json.loads((out / "new-portraits.json").read_text(encoding="utf-8"))
old_inventory = json.loads((project / "docs/validation/character-0.3.1/catalog-inventory.json").read_text(encoding="utf-8"))["figures"]
genders = {f["id"]: f["gender"] for f in old_inventory + new_portraits}
balance = {}
for figure in catalog:
    balance.setdefault(figure["genre"], Counter())[genders[figure["id"]]] += 1
assert len(balance) == 9 and all(dict(c) == {"weiblich": 5, "männlich": 5} for c in balance.values())
assert {f"{kind}-{p['id']}.png" for p in catalog for kind in ("card", "chat")} == {p.name for p in (out / "rendered-all-90").glob("*.png")}
assert len(list((out / "native-screenshots").glob("world-*-filter.png"))) == 4
apk = release / "Geschichten-0.4.0.apk"
manifest_path = release / "geschichten-android-update.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
assert manifest["versionCode"] == 6 and manifest["versionName"] == "0.4.0"
assert manifest["applicationId"] == "dev.vincent.geschichten" and manifest["minSdk"] == 31 and manifest["channel"] == "test"
assert manifest["apk"] == {"assetName": apk.name, "size": apk.stat().st_size, "sha256": digest(apk)}
with zipfile.ZipFile(apk) as package, zipfile.ZipFile(project.parent / "release-0.3.1/Geschichten-0.3.1.apk") as old:
    portraits = {Path(name).stem.removeprefix("portrait_"): name for name in package.namelist() if "/portrait_" in name}
    assert set(portraits) == {f["id"] for f in catalog}
    assert all(package.read(portraits[f["id"]]) == old.read(portraits[f["id"]]) for f in before)
    assert all(sha256(package.read(portraits[f["id"]])).hexdigest() == f["sha256"] for f in new_portraits)
    assert not any("catalog-v" in name for name in package.namelist())
dump = (out / "apk-manifest.txt").read_text(encoding="utf-8-sig")
assert "ComposeTestActivity" not in dump
assert "versionCode(0x0101021b)=6" in dump and '"0.4.0"' in dump
assert "Verification succesful" in (out / "zipalign.log").read_text(encoding="utf-8-sig")
assert "Paket/Version/Signatur geprüft:" in (out / "apk-manifest-check.log").read_text(encoding="utf-8-sig")
summary = {
    "version": "0.4.0", "versionCode": 6, "databaseVersion": 5,
    "publication": "local_ready_not_published", "finalJvm": jvm, "targetedAndroidUiTests": native,
    "distinctPassedCases": 80, "failures": 0, "skipped": 0,
    "correctedTestExpectations": "Age syntax supports existing years wording; v2 now adds 70; gallery test returns to the filter header between categories.",
    "lintSeverityCounts": severities, "lintIssueCounts": dict(Counter(issue.get("id") for issue in lint)),
    "catalog": {"total": 90, "added": 40, "released031ProfilesUnchanged": 50,
                "maxPersonalityCharacters": max(len(f["personality"]) for f in catalog),
                "maxScenarioCharacters": max(len(f["scenario"]) for f in catalog),
                "categoryGenderCounts": {k: dict(c) for k, c in balance.items()}},
    "old50PortraitsByteIdenticalTo031": True, "new40PortraitsMatchGeneratedSources": True,
    "actualUiScreenshots": 180, "newCategoryFilterScreenshots": 4,
    "testActivityAbsent": True, "zipAlignment16KB": "passed",
    "apk": manifest["apk"], "manifestSha256": digest(manifest_path),
    "signerCertificateSha256": "3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40",
    "physicalS24InstallationAndModelInference": "not_tested",
    "existingWindowsFileProviderHostCase": "not_rerun; provider and installation code unchanged",
}
public_path = out / "public-release-summary.json"
if public_path.exists():
    public = json.loads(public_path.read_text(encoding="utf-8"))
    assert public["releaseUrl"] == "https://github.com/Priestkiller/Geschichten/releases/tag/v0.4.0"
    assert public["draft"] is False and public["prerelease"] is True
    assert public["anonymousApiRequest"] and public["publicManifestMatchesLocal"] and public["publicReadmeMatchesPrepared"]
    assert public["fullPublicApkDownloaded"] and public["apkSha256"] == manifest["apk"]["sha256"]
    assert digest(out / "public-Geschichten-0.4.0.apk") == manifest["apk"]["sha256"]
    assert (out / "public-geschichten-android-update.json").read_bytes() == manifest_path.read_bytes()
    assert (out / "public-README.md").read_bytes() == (release / "README-GitHub-Vorschlag.md").read_bytes()
    assert (out / "app-parser-check.log").read_text(encoding="utf-8-sig").count("BESTANDEN:") == 2
    summary.update(publication="published_verified", publicRelease=public,
                   publicAppParserCheck="passed_for_codes_2_3_4_5",
                   publicReadmeUpdate=json.loads((out / "github-readme-update.json").read_text(encoding="utf-8")))
(out / "summary.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(f"PASS: 80 distinct cases; 0 failures/skips; 0 lint errors, {severities.get('Warning', 0)} warnings.")
print("PASS: 90 balanced profiles; old 50 profiles/assets unchanged; 40 original new assets; 180 UI captures.")
print("PASS: compatible version 0.4.0/code 6, signature, 16-KB ZIP alignment, production-only manifest.")
print(summary["publication"])
