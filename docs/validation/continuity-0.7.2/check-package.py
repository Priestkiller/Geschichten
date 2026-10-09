"""Verify the final 0.7.2 package and record the actual validation scope."""
from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET
import zipfile

output = Path(__file__).resolve().parent
root = output.parents[2]
release = root.parent / 'release-0.7.2'
apk = release / 'Geschichten-0.7.2.apk'
manifest = json.loads((release / 'geschichten-android-update.json').read_text())
assert manifest['versionCode'] == 11 and manifest['versionName'] == '0.7.2'
assert apk.stat().st_size == manifest['apk']['size']
assert hashlib.sha256(apk.read_bytes()).hexdigest() == manifest['apk']['sha256']

def tests(folder):
    result = dict(tests=0, failures=0, errors=0, skipped=0)
    for report in folder.glob('TEST-*.xml'):
        suite = ET.parse(report).getroot()
        for key in result:
            result[key] += int(suite.get(key, 0))
    assert result['tests'] > 0 and not any(result[key] for key in ('failures', 'errors', 'skipped'))
    return result

native = []
with zipfile.ZipFile(apk) as current, zipfile.ZipFile(root.parent / 'release-0.7.1/Geschichten-0.7.1.apk') as previous:
    names = current.namelist()
    assert not any(name.endswith(('.gguf', '.litertlm', '.keystore', '.kt', '.java')) for name in names)
    assert not any('robolectric' in name.lower() for name in names)
    for name in names:
        if name.endswith('.so'):
            assert current.read(name) == previous.read(name), name
            native.append(name)
assert len(native) == 3

counts = {}
for issue in ET.parse(output / 'final-verified-lint-results.xml').getroot().findall('issue'):
    severity = issue.get('severity')
    counts[severity] = counts.get(severity, 0) + 1
assert counts.get('Error', 0) == 0
result = dict(
    versionName='0.7.2', versionCode=11, apkBytes=apk.stat().st_size,
    apkSha256=manifest['apk']['sha256'],
    unitTests=tests(output / 'final-verified-jvm-results'),
    androidUiTests=tests(output / 'verified-ui-results'),
    lint=counts, nativeLibrariesUnchangedFrom071=native,
    modelFilesUnchanged=True, modelWeightsBundled=False,
    completeOpeningTextsRetained=True, sharedPromptForAll90AndCustomCharacters=True,
    sharedPromptForAllSixModels=True, physicalDeviceInstallTested=False,
    nativeProbe='final-verified-gemma-answer.json',
    storyQualityGuaranteed=False,
    nativeQualityFinding='The real model output remains inconsistent; the final Gemma probe asks another broad question instead of concretely specifying the register passage. Context regressions are covered by structural tests, not by a claimed six-model story-quality pass.',
    publication='prepared',
)
(output / 'summary.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result, indent=2))
