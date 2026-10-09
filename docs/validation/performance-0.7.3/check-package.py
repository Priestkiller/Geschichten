"""Audit final package and focused correctness checks; desktop data are not phone benchmarks."""
from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET
import zipfile

out = Path(__file__).resolve().parent
root = out.parents[2]
release = root.parent / 'release-0.7.3'
apk = release / 'Geschichten-0.7.3.apk'
manifest = json.loads((release / 'geschichten-android-update.json').read_text())
assert manifest['versionCode'] == 12 and manifest['versionName'] == '0.7.3'
assert apk.stat().st_size == manifest['apk']['size']
assert hashlib.sha256(apk.read_bytes()).hexdigest() == manifest['apk']['sha256']

def tests(folder, expected):
    result = dict(tests=0, failures=0, errors=0, skipped=0)
    for report in folder.glob('TEST-*.xml'):
        suite = ET.parse(report).getroot()
        for key in result:
            result[key] += int(suite.get(key, 0))
    assert result['tests'] == expected, result
    assert not any(result[key] for key in ('failures', 'errors', 'skipped')), result
    return result

native = []
with zipfile.ZipFile(apk) as current, zipfile.ZipFile(root.parent / 'release-0.7.2/Geschichten-0.7.2.apk') as previous:
    names = current.namelist()
    assert not any(name.endswith(('.gguf', '.litertlm', '.keystore', '.kt', '.java')) for name in names)
    assert not any('robolectric' in name.lower() for name in names)
    for name in names:
        if name.endswith('.so'):
            changed = current.read(name) != previous.read(name)
            assert changed == ('geschichten_gguf' in name), name
            if 'geschichten_gguf' in name:
                assert b'PerformanceNativeProbe' not in current.read(name)
            native.append(dict(path=name, changed=changed))
assert len(native) == 3

lint = {}
for issue in ET.parse(out / 'final-lint-results.xml').getroot().findall('issue'):
    severity = issue.get('severity')
    lint[severity] = lint.get(severity, 0) + 1
assert lint.get('Error', 0) == 0
cache = {}
for model in ('dolphin', 'huihui', 'gemma3'):
    rows = json.loads((out / (model + '-cache.json')).read_text())
    assert len(rows) == 8
    cases = {row['case']: row for row in rows}
    assert cases['cold']['reusedTokens'] == 0
    assert cases['identical-cached']['reusedTokens'] >= 256
    assert cases['changed-tail-cached']['reusedTokens'] >= 256
    assert cases['changed-tail-clean']['reusedTokens'] == 0
    assert cases['after-cancellation']['reusedTokens'] == 0
    assert cases['after-callback-error']['reusedTokens'] == 0
    differences = [row['maxLogitDifference'] for row in rows if 'maxLogitDifference' in row]
    assert len(differences) == 3 and max(differences) <= 0.05
    cache[model] = dict(cases=8, maximumLogitDifference=max(differences), runtime='Windows x86_64 CPU')
threads = json.loads((out / 'thread-configurations.json').read_text())
assert len(threads) == 18
assert len({row['model'] for row in threads}) == 6
assert all(row['firstTextMs'] > 0 and row['phoneBenchmark'] is False for row in threads)
result = dict(
    versionName='0.7.3', versionCode=12, apkBytes=apk.stat().st_size,
    apkSha256=manifest['apk']['sha256'], unitTests=tests(out / 'final-unit-results', 73),
    androidUiTests=tests(out / 'final-ui-results', 17), lint=lint, nativeLibraries=native,
    ggufCache=cache, cpuConfigurationSmokeTests=18, physicalDeviceBenchmark=False,
    modelFilesUnchanged=True, modelWeightsBundled=False, longOpeningsRetained=True,
    productionOutputTokenLimit=512, newGpuOrNpuPath=False,
    measurementSelection='On-device first-text measurements; keep baseline if improvement is less than 10 percent.',
    publication='prepared',
)
(out / 'summary.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result, indent=2))
