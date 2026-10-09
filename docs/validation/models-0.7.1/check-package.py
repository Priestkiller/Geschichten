"""Record focused validation for the description-only 0.7.1 update."""
from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET
import zipfile

root = Path(__file__).resolve().parents[3]
output = Path(__file__).resolve().parent
release = root.parent / 'release-0.7.1'
apk = release / 'Geschichten-0.7.1.apk'
manifest = json.loads((release / 'geschichten-android-update.json').read_text())
assert manifest['versionCode'] == 10 and manifest['versionName'] == '0.7.1'
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
with zipfile.ZipFile(apk) as current, zipfile.ZipFile(root.parent / 'release-0.7.0/Geschichten-0.7.0.apk') as previous:
    names = current.namelist()
    assert not any(name.endswith(('.gguf', '.litertlm', '.keystore', '.kt', '.java')) for name in names)
    for name in names:
        if name.endswith('.so'):
            assert current.read(name) == previous.read(name), name
            native.append(name)

lint = ET.parse(output / 'lint-results-debug.xml').getroot()
counts = {}
for issue in lint.findall('issue'):
    severity = issue.get('severity')
    counts[severity] = counts.get(severity, 0) + 1
assert counts.get('Error', 0) == 0
result = dict(
    versionName='0.7.1', versionCode=10, apkBytes=apk.stat().st_size,
    apkSha256=manifest['apk']['sha256'],
    unitTests=tests(output / 'unit-test-results'),
    androidUiTests=tests(output / 'visual-test-results'),
    lint=counts, nativeLibrariesUnchangedFrom070=native,
    modelFilesAndRuntimeUnchanged=True, modelWeightsBundled=False,
    nativeInferenceEvidence='../models-0.7.0', physicalDeviceInstallTested=False,
    publication='prepared',
)
(output / 'summary.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result, indent=2))
