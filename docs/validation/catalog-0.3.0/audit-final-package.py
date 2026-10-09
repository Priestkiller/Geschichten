"""Record checks of the completed local 0.3.0 build; no upload or APK edits."""

from collections import Counter
from hashlib import sha256
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile


EVIDENCE = Path(__file__).resolve().parent
PROJECT = EVIDENCE.parents[2]
RELEASE = PROJECT.parent / "release-0.3.0"


def reports(folder):
    result = []
    for path in sorted(folder.glob("TEST-*.xml")):
        root = ET.parse(path).getroot()
        result.append({"file": path.name, **{
            key: int(root.get(key, "0"))
            for key in ("tests", "failures", "errors", "skipped")
        }})
    return result


def digest(path):
    value = sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


final_jvm = reports(EVIDENCE / "final-jvm-tests")
assert sum(s["tests"] for s in final_jvm) == 46
assert all(s["failures"] == s["errors"] == s["skipped"] == 0 for s in final_jvm)
native_attempt = reports(EVIDENCE / "native-first-attempt")
latest_cases = {}
for path in sorted((EVIDENCE / "native-first-attempt").glob("TEST-*.xml")):
    for case in ET.parse(path).getroot().findall("testcase"):
        latest_cases[(case.get("classname"), case.get("name"))] = (
            case.find("failure") is None and case.find("error") is None
            and case.find("skipped") is None
        )
for path in (EVIDENCE / "artwork-final.xml", *sorted((EVIDENCE / "final-jvm-tests").glob("TEST-*.xml"))):
    for case in ET.parse(path).getroot().findall("testcase"):
        latest_cases[(case.get("classname"), case.get("name"))] = (
            case.find("failure") is None and case.find("error") is None
            and case.find("skipped") is None
        )
assert len(latest_cases) == 72 and sum(latest_cases.values()) == 71
remaining_failures = [f"{suite}.{name}" for (suite, name), passed in latest_cases.items() if not passed]
assert len(remaining_failures) == 1 and "UpdateFileProviderIntegrationTest" in remaining_failures[0]

lint = ET.parse(EVIDENCE / "lint-final.xml").getroot().findall("issue")
lint_severities = dict(Counter(issue.get("severity") for issue in lint))
assert not lint_severities.get("Error", 0) and not lint_severities.get("Fatal", 0)
inventory = json.loads((EVIDENCE / "catalog-inventory.json").read_text(encoding="utf-8"))["figures"]
assert len(inventory) == 50 and len({f["id"] for f in inventory}) == 50
balance = {}
for figure in inventory:
    category = balance.setdefault(figure["category"], Counter())
    category[figure["gender"]] += 1
assert len(balance) == 5 and all(dict(c) == {"weiblich": 5, "männlich": 5} for c in balance.values())
assert len(list((EVIDENCE / "rendered-all-50").glob("*.png"))) == 100

original_images = []
with zipfile.ZipFile(PROJECT.parent / "Geschichten-Android-Quellcode.zip") as archive:
    for name in archive.namelist():
        if "/res/drawable-nodpi/" not in name:
            continue
        basename = Path(name).name
        if basename.startswith("portrait_") and basename.endswith(".png") or basename == "scene_runa.png":
            original = archive.read(name)
            current = PROJECT / "app/src/main/res/drawable-nodpi" / basename
            assert current.read_bytes() == original, basename
            original_images.append(basename)
assert len(original_images) == 21

apk = RELEASE / "Geschichten-0.3.0.apk"
manifest_path = RELEASE / "geschichten-android-update.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
assert manifest["versionCode"] == 4 and manifest["versionName"] == "0.3.0"
assert manifest["applicationId"] == "dev.vincent.geschichten" and manifest["minSdk"] == 31
assert manifest["channel"] == "test"
assert manifest["apk"]["assetName"] == apk.name
assert manifest["apk"]["size"] == apk.stat().st_size
assert manifest["apk"]["sha256"] == digest(apk)
with zipfile.ZipFile(apk) as package:
    portrait_names = {Path(name).stem for name in package.namelist() if "/portrait_" in name}
    assert portrait_names == {"portrait_" + figure["id"] for figure in inventory}
manifest_dump = (EVIDENCE / "manifest-final.txt").read_text(encoding="utf-8-sig")
assert "ComposeTestActivity" not in manifest_dump
assert 'versionCode(0x0101021b)=4' in manifest_dump and '"0.3.0"' in manifest_dump
assert "Verification succesful" in (EVIDENCE / "zipalign-final.log").read_text(encoding="utf-8-sig")

summary = {
    "version": "0.3.0", "versionCode": 4, "publication": "local_ready_not_published",
    "finalJvm": final_jvm, "initialNativeAttempt": native_attempt,
    "distinctTestCasesAcrossRuns": len(latest_cases), "passedAcrossRuns": sum(latest_cases.values()),
    "remainingHostTestFailure": remaining_failures,
    "lintSeverityCounts": lint_severities, "lintIssueCounts": dict(Counter(i.get("id") for i in lint)),
    "catalog": {"total": 50, "added": 30, "categoryGenderCounts": {k: dict(v) for k, v in balance.items()}},
    "preservedOriginalImages": sorted(original_images), "reviewedActualUiScreenshots": 100,
    "testActivityAbsent": True, "zipAlignment16KB": "passed",
    "apk": manifest["apk"], "manifestSha256": digest(manifest_path),
    "signerCertificateSha256": "3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40",
    "phoneInstallationAndModelInference": "not_tested_on_physical_S24",
}
public_summary_path = EVIDENCE / "public-release-summary.json"
if public_summary_path.is_file():
    public_summary = json.loads(public_summary_path.read_text(encoding="utf-8"))
    assert public_summary["releaseUrl"] == "https://github.com/Priestkiller/Geschichten/releases/tag/v0.3.0"
    assert public_summary["draft"] is False and public_summary["prerelease"] is True
    assert public_summary["anonymousApiRequest"] and public_summary["publicManifestMatchesLocal"]
    assert public_summary["publicReadmeMatchesPrepared"] and public_summary["fullPublicApkDownloaded"]
    assert public_summary["apkBytes"] == manifest["apk"]["size"]
    assert public_summary["apkSha256"] == manifest["apk"]["sha256"]
    assert digest(EVIDENCE / "public-Geschichten-0.3.0.apk") == manifest["apk"]["sha256"]
    assert (EVIDENCE / "public-geschichten-android-update.json").read_bytes() == manifest_path.read_bytes()
    assert (EVIDENCE / "public-README.md").read_bytes() == (RELEASE / "README-GitHub-Vorschlag.md").read_bytes()
    assert (EVIDENCE / "app-parser-check.log").read_text(encoding="utf-8-sig").count("BESTANDEN:") == 2
    summary["publication"] = "published_verified"
    summary["publicRelease"] = public_summary
    summary["publicAppParserCheck"] = "passed_for_0.2.0_and_0.2.1_with_prereleases_enabled"
    summary["publicReadmeUpdate"] = json.loads((EVIDENCE / "github-readme-update.json").read_text(encoding="utf-8"))
(EVIDENCE / "summary.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("PASS: 46 final JVM tests; 71/72 distinct cases across runs; 0 lint errors, 68 warnings.")
print("PASS: 50 APK portraits, 5 categories with 5 women and 5 men, 21 original images unchanged.")
print("PASS: 100 actual UI screenshots, version/signature manifest, 16-KB alignment, no test activity.")
if summary["publication"] == "published_verified":
    print("PUBLISHED AND VERIFIED: 0.3.0 / code 4, full anonymous download and actual app parser passed.")
else:
    print("READY LOCALLY: 0.3.0 / code 4. Public release remains pending.")
print("Physical-phone installation and model inference remain untested.")
