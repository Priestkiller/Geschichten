"""Collect reproducible audit counters/hashes without reading any private chats."""
import collections, difflib, hashlib, json, pathlib, shutil, xml.etree.ElementTree as ET, zipfile
AUDIT=pathlib.Path(__file__).resolve().parent
ROOT=AUDIT.parents[2]
def read(path):return json.loads(path.read_text(encoding='utf-8'))
def sha(path):
    h=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(4*1024*1024),b''):h.update(block)
    return h.hexdigest()
def tests(directory):
    suites=[]
    for path in sorted(directory.glob('TEST-*.xml')):
        x=ET.parse(path).getroot()
        suites.append({'name':x.attrib['name'],**{k:int(x.attrib[k]) for k in ['tests','failures','errors','skipped']}})
    return {'suites':suites,'totals':{k:sum(x[k] for x in suites) for k in ['tests','failures','errors','skipped']}}
dest=AUDIT/'final-results';dest.mkdir(exist_ok=True)
for path in (ROOT/'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'):shutil.copy2(path,dest/path.name)
shutil.copy2(ROOT/'app/build/reports/lint-results-debug.xml',AUDIT/'final-lint.xml')
original=read(AUDIT/'source-before.json');changed=[];removed=[]
with zipfile.ZipFile(AUDIT/'source-before.zip') as archive:
    for row in original:
        path=ROOT/row['Path'];key=row['Path'].replace('\\','/')
        if not path.exists():removed.append(key);continue
        if sha(path).lower()!=row['SHA256'].lower():
            changed.append(key)
            if path.suffix in ['.kt','.cpp','.kts']:
                backup_key=key.removeprefix('app/')
                old=archive.read(backup_key).decode('utf-8-sig').splitlines(keepends=True)
                new=path.read_text(encoding='utf-8-sig').splitlines(keepends=True)
                (AUDIT/(path.name+'.diff')).write_text(''.join(difflib.unified_diff(old,new,fromfile='before/'+key,tofile='after/'+key)),encoding='utf-8')
old_paths={row['Path'].replace('\\','/') for row in original}
new=sorted(str(p.relative_to(ROOT)).replace('\\','/') for p in (ROOT/'app/src').rglob('*') if p.is_file() and str(p.relative_to(ROOT)).replace('\\','/') not in old_paths)
def lint(path):
    items=list(ET.parse(path).getroot().findall('issue'))
    return {'bySeverity':dict(collections.Counter(i.attrib['severity'] for i in items)),
            'byId':dict(collections.Counter(i.attrib['id'] for i in items))}
apk=ROOT/'app/build/outputs/apk/debug/app-debug.apk'
with zipfile.ZipFile(apk) as archive:
    apkmeta={'sha256':sha(apk),'bytes':apk.stat().st_size,'nativeLibraries':[p for p in archive.namelist() if p.endswith('.so')],
             'includesSyntheticTestClass':any(b'ChatContextIntegrationTest' in archive.read(p) or b'FakeGeneration' in archive.read(p) for p in archive.namelist() if p.endswith('.dex'))}
adapter=ROOT.parent/'training/geschichten-pilot-v1/runs/geschichten-qwen3-4b-v1/adapter/adapter_model.safetensors'
out={'changedExistingSourceFiles':changed,'newSourceFiles':new,'removedOriginalSourceFiles':removed,
     'normalTests':tests(dest),'selectedIntegrationTests':tests(AUDIT/'integration-results'),
     'lintBefore':lint(AUDIT/'baseline-lint.xml'),'lintAfter':lint(AUDIT/'final-lint.xml'),
     'apk':apkmeta,'adapter':{'sha256':sha(adapter),'bytes':adapter.stat().st_size},
     'inputAfterSha256':sha(AUDIT/'inputs-final.json'),'inputBeforeSha256':sha(AUDIT/'inputs-before.json')}
(AUDIT/'audit-evidence.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:out[k] for k in ['changedExistingSourceFiles','newSourceFiles','removedOriginalSourceFiles','normalTests','selectedIntegrationTests','lintBefore','lintAfter','apk']},ensure_ascii=True,indent=2))
