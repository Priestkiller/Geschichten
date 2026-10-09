from pathlib import Path
import json

output = Path(__file__).resolve().parent
project = output.parents[2]
previous = output.parent / 'models-0.7.1'
manifest = json.loads((project.parent / 'release-0.7.2/geschichten-android-update.json').read_text())
source = (previous / 'verify-public-release.py').read_text(encoding='utf-8')
source = source.replace('0.7.1', '0.7.2')
source = source.replace('e9a1fe48bf2692d2a6dd05078d67e2d844fd66c2498e7687c6d1302b1b4225a8', manifest['apk']['sha256'])
source = source.replace('== 10 > 9', '== 11 > 10 > 9')
source = source.replace('313286124', str(manifest['apk']['size']))
source = source.replace('verify=171026b02247723b6e5d999a461cd6bfdb512d8b', 'verify=' + manifest['apk']['sha256'])
(output / 'verify-public-release.py').write_text(source, encoding='utf-8', newline='\n')
java = (previous / 'UpdateReleaseProbe.java').read_text(encoding='utf-8')
java = java.replace('0.7.1', '0.7.2').replace('0.7.0', '0.7.1')
java = java.replace('getVersionCode() != 10', 'getVersionCode() != 11').replace('getVersionCode() <= 9', 'getVersionCode() <= 10')
java = java.replace('Code 10', 'Code 11').replace('Code 9', 'Code 10')
(output / 'UpdateReleaseProbe.java').write_text(java, encoding='utf-8', newline='\n')
print('Anonyme vollständige Releaseprüfung und unveränderter App-Parser für 0.7.2 vorbereitet.')
