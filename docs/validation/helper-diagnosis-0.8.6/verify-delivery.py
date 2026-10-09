from pathlib import Path
import json,hashlib,zipfile,xml.etree.ElementTree as E
p=Path(__file__).resolve().parent;root=p.parents[2]
def sha(f):
    h=hashlib.sha256()
    with f.open('rb') as s:
        for b in iter(lambda:s.read(1048576),b''):h.update(b)
    return h.hexdigest()
def counts(folder):
    rows=[E.parse(f).getroot() for f in (p/folder).rglob('TEST-*.xml')]
    return {k:sum(int(r.attrib[k]) for r in rows) for k in ('tests','failures','errors','skipped')}, {(r.attrib['name'],t.attrib['name']) for r in rows for t in r.findall('testcase')}
normal,ns=counts('regular-results');integration,ins=counts('integration-results');sql,_=counts('ownership-persistence-results')
assert normal==dict(tests=148,failures=0,errors=0,skipped=0)
assert integration==dict(tests=100,failures=0,errors=0,skipped=0)
assert sql==dict(tests=1,failures=0,errors=0,skipped=0)
lint={v:sum(i.attrib['severity']==v for i in E.parse(p/'lint-final.xml').getroot()) for v in ('Error','Warning','Information')}
assert lint==dict(Error=0,Warning=133,Information=1)
baseline=json.loads((p/'baseline-hashes.json').read_text(encoding='utf-8'))
originals=[r for r in baseline if r['path'].startswith('docs/validation/team-0.8.6/') or r['path']=='Geschichten-0.8.6.apk']
assert all(sha(root/r['path'])==r['sha256'] for r in originals)
changed=[r['path'] for r in baseline if r['path'].startswith(('app/src/','app/build.gradle.kts','BUILD_WINDOWS.cmd','README.md')) and sha(root/r['path'])!=r['sha256']]
oldset={r['path'] for r in baseline}
changed.extend(f.relative_to(root).as_posix() for f in (root/'app/src').rglob('*') if f.is_file() and f.relative_to(root).as_posix() not in oldset)
assert set(changed)==set(['app/src/main/java/dev/vincent/geschichten/memory/RoleEvidence.kt','app/src/test/java/dev/vincent/geschichten/memory/OwnershipAdverbTest.kt','app/src/visualTest/java/dev/vincent/geschichten/memory/OwnershipAdverbPersistenceTest.kt','app/build.gradle.kts','BUILD_WINDOWS.cmd','README.md']),changed
modelgold=json.loads((root/'docs/validation/team-0.8.6/delivery-verification.json').read_text(encoding='utf-8'))['modelFiles']
models={}
for name,gold in modelgold.items():
    f=next(x for x in [root/'docs/validation/quality-search-0.8.3/model-files'/name,root/'docs/validation/models-0.7.0/model-files'/name] if x.exists())
    h=sha(f);assert h==gold['sha256'];models[name]=dict(bytes=f.stat().st_size,sha256=h,unchanged=True)
cases=json.loads((p/'cases-frozen.json').read_text(encoding='utf-8'));caseById={c['id']:c for c in cases}
assert sum(c['split']=='acceptance' for c in cases)==12
sourceRows=json.loads((root/'docs/validation/team-0.8.6/phase-b-scenes-frozen.json').read_text(encoding='utf-8'))
for c in cases:
    if c['split']=='development':
        origin=c['origin'];row=next(r for r in sourceRows if r['case']==origin['case']);message=next(m for m in row['bundle']['messages'] if m['id']==origin['sourceId'])
        assert message['text'][origin['start']:origin['end']]==c['source']['text']
    assert hashlib.sha256(c['source']['text'].encode()).hexdigest()==c['source']['sourceRevision']
total=tokenChecks=0;rawFiles=[]
for f in p.glob('raw-*.json'):
    rows=json.loads(f.read_text(encoding='utf-8'));rawFiles.append(dict(file=f.name,sha256=sha(f)))
    for r in rows:
        total+=1;tokenChecks+=1;assert r['completed'];assert r['inputTokens']+512<=4096;assert r['inputTokens']==r['nativePromptTokens']
        c=caseById[r['case']]
        assert r['appBinding']==c['source']
        assert r['user']=='Original S1 ('+c['speaker']+' spricht mit '+c['addressee']+'):\n'+c['source']['text']+'\n'+('Behauptung: '+c['claim'] if r['mode']=='C' else 'Frage: '+c['question'])
        assert 'story' not in r['system'] and 'request' not in r['system'] and 'version' not in r['system']
        assert r['temperature'] in (.75,.2)
assert total==132 and tokenChecks==132
before=json.loads((p/'guard-before.json').read_text(encoding='utf-8'));after=json.loads((p/'guard-after.json').read_text(encoding='utf-8'))
assert before['drafts'][0]['wholeUneditedReply']==after['drafts'][0]['wholeUneditedReply']
assert not before['drafts'][0]['guardAccepted'] and after['drafts'][0]['guardAccepted']
assert 'nicht weiterhin' in before['drafts'][0]['error']
stored=json.loads((p/'repaired-draft-stored.json').read_text(encoding='utf-8'))
assert stored['messages'][-1]['text']==before['drafts'][0]['wholeUneditedReply']
assert any(f['manual'] and f['field']=='owner' and f['value']=='Oda' and f['status']=='CURRENT' for f in stored['memory']['facts'])
apkpath=root/'Geschichten-0.8.7.apk'
with zipfile.ZipFile(apkpath) as apk,zipfile.ZipFile(root/'Geschichten-0.8.6.apk') as oldapk:
    shared=[n for n in oldapk.namelist() if n.startswith(('lib/','assets/'))]
    assert all(apk.read(n)==oldapk.read(n) for n in shared)
    assert not any('validation' in n or n.endswith('.litertlm') or (n.endswith('.gguf') and not n.startswith('assets/tokenizers/')) for n in apk.namelist())
    for n in apk.namelist():
        if n.endswith('.dex'):
            for s in (b'SimpleProbe',b'cases-frozen.json',b'OwnershipAdverbTest',b'OwnershipAdverbPersistenceTest',b'original-new-gift'):
                assert s not in apk.read(n),s
signer='3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40'
assert signer in (p/'apksigner.txt').read_text()
assert 'Verified using v2 scheme (APK Signature Scheme v2): true' in (p/'apksigner.txt').read_text()
assert (p/'zipalign.txt').read_text().strip().endswith('Verification succesful')
assert "versionCode='21' versionName='0.8.7'" in (p/'apk-badging.txt').read_text()
assert 'SQLiteOpenHelper(context, "geschichten.db", null, 10)' in (root/'app/src/main/java/dev/vincent/geschichten/data/StoryRepository.kt').read_text()
assert 'absolutePath,null,1)' in (root/'app/src/main/java/dev/vincent/geschichten/memory/SemanticIndex.kt').read_text()
info=dict(apk=dict(file=apkpath.name,bytes=apkpath.stat().st_size,sha256=sha(apkpath),versionCode=21,versionName='0.8.7',sameSignerAs086=True,signerSha256=signer,v2=True,aligned16KiB=True,byteEqualNativeAndAssetFiles=len(shared),diagnosticCodeAndWeightsNotPackaged=True),
    baselinePreservedFiles=len(originals),changedProductionAndTestFiles=changed,modelFiles=models,database=dict(schema=10,indexSchema=1,changed=False),
    tests=dict(regular=normal,integration=integration,overlap=len(ns&ins),additionalRealDraftSql=sql,isolatedBindingTests=4),lint=lint,
    realNativeGenerations=total,exactOwnTokenizerChecks=tokenChecks,profiles=dict(baselineTemperature=.75,additionalTemperature=.2,topP=.9,topK=40,repeatPenalty=1.08,repeatWindow=256,seed=42,context=4096,output=512,threads=4),
    independentAcceptance=dict(cases=12,AContent=8,BContent=8,CContent=9,forcedBContent=8,CFalseApproval=1,CFalseComplaint=2),
    helperReintegrated=False,approachEndedForTestedCandidates=True,additionalAgents=False,defaultTeamEnabled=False,modelChoiceChanged=False,cloud=False,training=False,newDownloads=False,published=False,
    s24='No ADB device; performance/RAM on S24 and S24 Ultra remain open',rawFiles=rawFiles,
    productionSourceHashes={f.relative_to(root).as_posix():sha(f) for f in (root/'app/src/main').rglob('*') if f.is_file()})
(p/'delivery-verification.json').write_text(json.dumps(info,ensure_ascii=False,indent=2),encoding='utf-8')
manifest=[dict(file=f.relative_to(p).as_posix(),sha256=sha(f)) for f in p.rglob('*') if f.is_file() and not any(part in f.parts for part in ('native-cpu','native-cpu-final','__pycache__')) and f.name!='artifact-hashes.json']
manifest.append(dict(file='native-cpu-final/geschichten_gguf.dll',sha256=sha(p/'native-cpu-final/geschichten_gguf.dll')))
(p/'artifact-hashes.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(dict(apk=info['apk'],tests=info['tests'],lint=lint,preserved=len(originals),actualNativeCalls=total,models=len(models)),ensure_ascii=False))
