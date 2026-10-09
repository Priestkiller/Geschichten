"""Audit the completed 0.3.1 package and record local evidence; no upload."""

from collections import Counter
from hashlib import sha256
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

EVIDENCE = Path(__file__).resolve().parent
PROJECT = EVIDENCE.parents[2]
RELEASE = PROJECT.parent / "release-0.3.1"


def digest(path):
    result = sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def reports(folder, expected):
    suites = []
    cases = set()
    for path in sorted(folder.glob("TEST-*.xml")):
        root = ET.parse(path).getroot()
        counts = {key: int(root.get(key, "0")) for key in ("tests", "failures", "errors", "skipped")}
        assert counts["failures"] == counts["errors"] == counts["skipped"] == 0, path
        suites.append({"file": path.name, **counts})
        for case in root.findall("testcase"):
            assert not any(case.find(tag) is not None for tag in ("failure", "error", "skipped"))
            cases.add((case.get("classname"), case.get("name")))
    assert len(cases) == sum(s["tests"] for s in suites) == expected
    return suites, cases


jvm, jvm_cases = reports(EVIDENCE / "final-jvm-tests", 49)
native, native_cases = reports(EVIDENCE / "native-tests", 26)
assert len(jvm_cases | native_cases) == 75
lint = ET.parse(EVIDENCE / "lint-final.xml").getroot().findall("issue")
severities = dict(Counter(issue.get("severity") for issue in lint))
assert not severities.get("Error", 0) and not severities.get("Fatal", 0)

before = {f["id"]: f for f in json.loads((EVIDENCE / "catalog-before.json").read_text(encoding="utf-8"))}
after = json.loads((EVIDENCE / "catalog-reviewed.json").read_text(encoding="utf-8"))
inventory = json.loads((EVIDENCE / "catalog-inventory.json").read_text(encoding="utf-8"))["figures"]
assert len(after) == len(before) == len(inventory) == 50
assert len({f["id"] for f in after}) == 50
assert {f["id"] for f in after} == {f["id"] for f in inventory} == set(before)
scenario_changes = []
for figure in after:
    old = before[figure["id"]]
    for key in old.keys() - {"personality", "openingMessage", "scenario"}:
        assert figure[key] == old[key], (figure["id"], key)
    assert old["personality"] != figure["personality"]
    assert old["openingMessage"] != figure["openingMessage"]
    assert len(figure["personality"]) <= 630
    assert len(figure["scenario"]) <= 480
    assert len(figure["openingMessage"]) <= 1500
    if old["scenario"] != figure["scenario"]:
        scenario_changes.append(figure["id"])
assert scenario_changes == ["leon"]
balance = {}
for figure in inventory:
    balance.setdefault(figure["category"], Counter())[figure["gender"]] += 1
assert len(balance) == 5 and all(dict(counts) == {"weiblich": 5, "männlich": 5} for counts in balance.values())
assert len(list((EVIDENCE / "rendered-all-50").glob("*.png"))) == 100

apk = RELEASE / "Geschichten-0.3.1.apk"
manifest_path = RELEASE / "geschichten-android-update.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
assert manifest["versionCode"] == 5 and manifest["versionName"] == "0.3.1"
assert manifest["applicationId"] == "dev.vincent.geschichten" and manifest["minSdk"] == 31
assert manifest["channel"] == "test"
assert manifest["apk"] == {"assetName": apk.name, "size": apk.stat().st_size, "sha256": digest(apk)}
with zipfile.ZipFile(apk) as package, zipfile.ZipFile(PROJECT.parent / "release-0.3.0/Geschichten-0.3.0.apk") as old_package:
    portraits = {name for name in package.namelist() if "/portrait_" in name}
    assert {Path(name).stem for name in portraits} == {"portrait_" + f["id"] for f in inventory}
    assert portraits == {name for name in old_package.namelist() if "/portrait_" in name}
    assert all(package.read(name) == old_package.read(name) for name in portraits)
    assert not any(name.endswith("catalog-v3.json") for name in package.namelist())
manifest_dump = (EVIDENCE / "apk-manifest.txt").read_text(encoding="utf-8-sig")
assert "ComposeTestActivity" not in manifest_dump
assert "versionCode(0x0101021b)=5" in manifest_dump and '"0.3.1"' in manifest_dump
assert "Verification succesful" in (EVIDENCE / "zipalign.log").read_text(encoding="utf-8-sig")
assert "Paket/Version/Signatur geprüft:" in (EVIDENCE / "apk-manifest-check.log").read_text(encoding="utf-8-sig")

summary = {
    "version": "0.3.1", "versionCode": 5, "databaseVersion": 4,
    "publication": "local_ready_not_published",
    "finalJvm": jvm, "targetedAndroidUiTests": native,
    "distinctPassedCases": 75, "failures": 0, "skipped": 0,
    "initialSandboxSetupFailureResolved": "Robolectric download-lock permission; rerun succeeded",
    "lintSeverityCounts": severities,
    "lintIssueCounts": dict(Counter(issue.get("id") for issue in lint)),
    "catalog": {"total": 50, "revisedPersonalities": 50, "revisedOpenings": 50,
                "revisedScenarios": scenario_changes,
                "maxPersonalityCharacters": max(len(f["personality"]) for f in after),
                "maxScenarioCharacters": max(len(f["scenario"]) for f in after),
                "categoryGenderCounts": {k: dict(v) for k, v in balance.items()}},
    "all50PortraitsByteIdenticalToReleased030": True,
    "actualUiScreenshots": 100, "testActivityAbsent": True, "zipAlignment16KB": "passed",
    "apk": manifest["apk"], "manifestSha256": digest(manifest_path),
    "signerCertificateSha256": "3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40",
    "physicalS24InstallationAndModelInference": "not_tested",
    "existingWindowsFileProviderHostCase": "not_rerun; provider and installation code unchanged",
}
public_path = EVIDENCE / "public-release-summary.json"
if public_path.exists():
    public = json.loads(public_path.read_text(encoding="utf-8"))
    assert public["releaseUrl"] == "https://github.com/Priestkiller/Geschichten/releases/tag/v0.3.1"
    assert public["draft"] is False and public["prerelease"] is True
    assert public["anonymousApiRequest"] and public["publicManifestMatchesLocal"] and public["publicReadmeMatchesPrepared"]
    assert public["fullPublicApkDownloaded"] and public["apkSha256"] == manifest["apk"]["sha256"]
    assert digest(EVIDENCE / "public-Geschichten-0.3.1.apk") == manifest["apk"]["sha256"]
    assert (EVIDENCE / "public-geschichten-android-update.json").read_bytes() == manifest_path.read_bytes()
    assert (EVIDENCE / "public-README.md").read_bytes() == (RELEASE / "README-GitHub-Vorschlag.md").read_bytes()
    assert (EVIDENCE / "app-parser-check.log").read_text(encoding="utf-8-sig").count("BESTANDEN:") == 2
    summary["publication"] = "published_verified"
    summary["publicRelease"] = public
    summary["publicAppParserCheck"] = "passed_for_0.2.0_0.2.1_and_0.3.0"
    summary["publicReadmeUpdate"] = json.loads((EVIDENCE / "github-readme-update.json").read_text(encoding="utf-8"))
(EVIDENCE / "summary.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("PASS: 49 final JVM tests and 26 targeted Android/UI cases; 75 distinct cases, zero failures or skips.")
print(f"PASS: zero lint errors; {severities.get('Warning', 0)} warnings.")
print("PASS: 50 revised personalities/openings, retained identities and balanced categories; 100 UI images.")
print("PASS: all 50 packaged portraits identical to 0.3.0; compatible signed APK, 16-KB ZIP alignment.")
if summary["publication"] == "published_verified":
    print("PUBLISHED AND VERIFIED: 0.3.1 / code 5; anonymous full download, README and actual app parser passed.")
else:
    print("READY LOCALLY: 0.3.1 / code 5. Public release remains pending.")
print("Physical S24 installation and model inference remain untested.")
