"""Record final tests, exact compiled prose and production APK identity."""
from pathlib import Path
import hashlib
import json
import re
import statistics
import xml.etree.ElementTree as ET
import zipfile
from collections import Counter

out = Path(__file__).resolve().parent
project = out.parents[2]
release = project.parent / 'release-0.6.0'
catalog = json.loads((out / 'catalog-final.json').read_text(encoding='utf-8'))
before = json.loads((out.parent / 'prologues-0.5.0/catalog-final.json').read_text(encoding='utf-8'))
assert len(catalog) == len(before) == 90
for old, new in zip(before, catalog):
    for key in old:
        if key not in {'openingMessage', 'personality', 'scenario'}:
            assert old[key] == new[key], (new['id'], key)
    assert 2800 <= len(new['openingMessage']) <= 8000
    assert len(new['personality']) <= 1800
    assert len(new['scenario']) <= 1000
assert len({p['openingMessage'] for p in catalog}) == 90
words = [len(re.findall(r'\S+', p['openingMessage'])) for p in catalog]
lengths = [len(p['openingMessage']) for p in catalog]

def reports(folder):
    result = []
    for path in sorted(folder.glob('*.xml')):
        root = ET.parse(path).getroot()
        record = {'file': path.name, **{key: int(root.get(key, '0')) for key in ('tests', 'failures', 'errors', 'skipped')}}
        assert record['tests'] > 0 and record['failures'] == record['errors'] == record['skipped'] == 0
        result.append(record)
    return result

jvm, native = reports(out / 'jvm-final'), reports(out / 'native-tests')
assert sum(r['tests'] for r in jvm) == 55
assert sum(r['tests'] for r in native) == 41
lint = ET.parse(out / 'lint-results-debug.xml').getroot()
severities = Counter(i.get('severity') for i in lint.findall('issue'))
assert not severities.get('Error') and not severities.get('Fatal')
manifest = json.loads((release / 'geschichten-android-update.json').read_text(encoding='utf-8'))
apk = release / manifest['apk']['assetName']
assert apk.stat().st_size == manifest['apk']['size']
assert hashlib.sha256(apk.read_bytes()).hexdigest() == manifest['apk']['sha256']
assert 'TestActivity' not in (out / 'apk-manifest.txt').read_text(encoding='utf-8')
with zipfile.ZipFile(apk) as current, zipfile.ZipFile(project.parent / 'release-0.5.0/Geschichten-0.5.0.apk') as old:
    portraits = [f'res/drawable-nodpi-v4/portrait_{p["id"]}.png' for p in catalog]
    scenes = [name for name in old.namelist() if '/scene_' in name and name.endswith('.png')]
    for entry in portraits + scenes:
        assert current.read(entry) == old.read(entry), entry
    assert not any('catalog-v' in name for name in current.namelist())
assert 'Verification succesful' in (out / 'zipalign.log').read_text(encoding='utf-8')
summary = {
    'version': '0.6.0', 'versionCode': 8, 'databaseVersion': 7, 'publication': 'prepared_verified',
    'finalJvm': jvm, 'targetedAndroidUiTests': native, 'distinctPassedCases': 96,
    'failures': 0, 'skipped': 0, 'lintSeverityCounts': dict(severities),
    'lintIssueCounts': dict(Counter(i.get('id') for i in lint.findall('issue'))),
    'catalog': {'total': 90, 'individualIntroductions': 90, 'minOpeningCharacters': min(lengths),
        'maxOpeningCharacters': max(lengths), 'averageOpeningCharacters': round(statistics.mean(lengths)),
        'minWords': min(words), 'maxWords': max(words), 'averageWords': round(statistics.mean(words)),
        'categoryCounts': dict(Counter(p['genre'] for p in catalog)),
        'identityPortraitsTraitsTitlesAndOrderUnchanged': True},
    'all90PortraitsAndExistingSceneImagesByteIdenticalTo050': True,
    'actualCharacterUiScreenshots': 450, 'visuallyReviewedMontages': 27,
    'navigation': ['Figuren', 'Verlauf', 'Erinnerungen', 'Einstellungen'],
    'newChatsStartAtIntroduction': True, 'playedChatsResumeAtLastMessage': True,
    'unplayedScaffoldMigrationAll90': 'passed; IDs, timestamps and message counts retained',
    'playedAndEditedArchives': 'preserved; no new player role injected',
    'testActivityAndTestFixturesAbsent': True, 'zipAlignment16KB': 'passed', 'apk': manifest['apk'],
    'manifestSha256': hashlib.sha256((release / 'geschichten-android-update.json').read_bytes()).hexdigest(),
    'signerCertificateSha256': '3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40',
    'physicalS24InstallationAndModelInference': 'not_tested',
    'existingWindowsFileProviderHostCase': 'not_rerun; provider and installation code unchanged',
    'initialCorrections': 'Shortened internal model instructions to preserve saved summaries; corrected seed timestamp fixture; made chat end marker measurable. Story introductions remain full length.'
}
(out / 'summary.json').write_text(json.dumps(summary, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
print(json.dumps({'passed': 96, 'lint': dict(severities), 'words': [min(words), max(words)], 'apk': manifest['apk']}, ensure_ascii=False))
