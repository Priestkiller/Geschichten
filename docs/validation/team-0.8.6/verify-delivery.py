from pathlib import Path
import json,hashlib,zipfile,xml.etree.ElementTree as E
root=Path(__file__).resolve().parents[3]
p=Path(__file__).resolve().parent
def sha(f):
    h=hashlib.sha256()
    with f.open('rb') as s:
        for b in iter(lambda:s.read(1024*1024),b''):h.update(b)
    return h.hexdigest()
def tests(folder):
    rows=[E.parse(f).getroot() for f in (p/folder).rglob('TEST-*.xml')]
    counts={k:sum(int(r.attrib[k]) for r in rows) for k in ('tests','failures','errors','skipped')}
    unique={(r.attrib['name'],t.attrib['name']) for r in rows for t in r.findall('testcase')}
    return counts,unique
normal,names=tests('regular-delivery-results');integration,inames=tests('integration-delivery-results')
assert normal==dict(tests=146,failures=0,errors=0,skipped=0),normal
assert integration==dict(tests=98,failures=0,errors=0,skipped=0),integration
overlap=len(names&inames)
lint={s:sum(i.attrib['severity']==s for i in E.parse(p/'lint-delivery.xml').getroot()) for s in ('Error','Warning','Information')}
assert lint==dict(Error=0,Warning=133,Information=1),lint
old=json.loads((p/'baseline-hashes.json').read_text(encoding='utf-8'))
old_art=[r for r in old if r['path'].startswith('docs/validation/answers-0.8.5/')]
assert all(sha(root/r['path'])==r['sha256'] for r in old_art)
oldmap={r['path']:r['sha256'] for r in old}
preserved=[]
for f in ('GroundedFacts.kt','MemoryDatabase.kt','RoleEvidence.kt','RoleReplyGuard.kt','MemoryReplyGuard.kt','MemoryPrompt.kt','SemanticSearch.kt','SemanticIndex.kt','ArchiveRecall.kt','HybridRecall.kt','FactAnswer.kt','FactRepair.kt','RoleSourcePolicy.kt'):
    name='app/src/main/java/dev/vincent/geschichten/memory/'+f
    assert sha(root/name)==oldmap[name],name
    preserved.append(name)
catalog='app/src/main/java/dev/vincent/geschichten/ai/LocalModelCatalog.kt'
assert sha(root/catalog)==oldmap[catalog]
prior=json.loads((root/'docs/validation/answers-0.8.5/delivery-verification.json').read_text(encoding='utf-8'))
weights={}
for name,gold in prior['modelFiles'].items():
    candidates=[root/'docs/validation/quality-search-0.8.3/model-files'/name,root/'docs/validation/models-0.7.0/model-files'/name]
    f=next(x for x in candidates if x.exists());digest=sha(f)
    assert digest==gold['sha256'],name
    weights[name]=dict(bytes=f.stat().st_size,sha256=digest,unchanged=True)
apkpath=root/'Geschichten-0.8.6.apk'
with zipfile.ZipFile(apkpath) as apk,zipfile.ZipFile(root/'Geschichten-0.8.5.apk') as priorapk:
    shared=[n for n in priorapk.namelist() if n.startswith(('lib/','assets/'))]
    assert all(apk.read(n)==priorapk.read(n) for n in shared)
    assert not any('validation' in n or n.endswith('.litertlm') or (n.endswith('.gguf') and not n.startswith('assets/tokenizers/')) for n in apk.namelist())
    for n in apk.namelist():
        if n.endswith('.dex'):
            for test in (b'raw-team-C.json',b'TeamConversationProbeTest',b'phase-b-C-dialogue',b'phase-a-complete-frozen.json'):
                assert test not in apk.read(n),test
signer='3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40'
assert signer in (p/'apksigner.txt').read_text() and 'Verified using v2 scheme (APK Signature Scheme v2): true' in (p/'apksigner.txt').read_text()
assert (p/'zipalign.txt').read_text().strip().endswith('Verification succesful')
assert "versionCode='20' versionName='0.8.6'" in (p/'apk-badging.txt').read_text()
assert 'false' in (root/'app/src/main/java/dev/vincent/geschichten/AppViewModel.kt').read_text().split('getBoolean("team_enabled",')[1].split(')')[0]
counts={};token_checks=0
for condition in ('A','B','C','self'):
    rows=json.loads((p/f'raw-team-{condition}.json').read_text(encoding='utf-8'))
    counts[condition]=sum(len(r['steps']) for r in rows)
    for r in rows:
        assert len(r['steps'])<=(1 if condition=='A' else 4 if condition=='B' else 5)
        for s in r['steps']:
            assert s['inputTokens']+512<=4096
            if s['completed']:assert s['inputTokens']==s['nativePromptTokens']
            token_checks+=1
    if condition!='A':
        replay=json.loads((p/f'final-controller-replay-{condition}.json').read_text(encoding='utf-8'))
        assert len(replay)==len(rows) and all(r['sameFinalDecisionAndAnswer'] and r['allInputsTokenCountsTemplatesIdentical'] for r in replay)
for model in ('huihui-qwen3-4b','gemma-4-e2b'):
    rows=json.loads((p/f'raw-phase-a-{model}.json').read_text(encoding='utf-8'));assert len(rows)==14
    counts[model]=len(rows)
    for r in rows:
        assert r['plannedInputTokens']+512<=4096
        if 'nativePromptTokens' in r:assert r['plannedInputTokens']==r['nativePromptTokens']
        token_checks+=1
for round in (1,2):
    row=json.loads((p/f'raw-dialogue-{round}.json').read_text(encoding='utf-8'))[0]
    assert row['completed']
    accepted=json.loads((p/f'dialogue-{round}-accepted.json').read_text(encoding='utf-8'))
    assert accepted['usable'] and accepted['atomicCommit'] and accepted['realAnswerUnchanged'] and accepted['originalsPreserved']
    stored=json.loads((p/f'dialogue-{round}-stored-after.json').read_text(encoding='utf-8'))
    assert stored['messages'][-1]['text']==row['answer']
    assert any(f['manual'] and f['field']=='owner' and f['value']=='Oda' and f['status']=='CURRENT' for f in stored['memory']['facts'])
    counts[f'dialogue-{round}']=len(row['steps'])
    for s in row['steps']:
        assert s['inputTokens']+512<=4096
        if s['completed']:assert s['inputTokens']==s['nativePromptTokens']
        token_checks+=1
assert sum(counts.values())==86 and token_checks==86
changed=[]
for r in old:
    if r['path'].startswith('app/src/') or r['path'] in ('app/build.gradle.kts','README.md','BUILD_WINDOWS.cmd'):
        if sha(root/r['path'])!=r['sha256']:changed.append(r['path'])
changed.extend(f.relative_to(root).as_posix() for f in (root/'app/src').rglob('*') if f.is_file() and f.relative_to(root).as_posix() not in oldmap)
changed.append('docs/PRUEFBERICHT-0.8.6.md')
(p/'changed-files.json').write_text(json.dumps(sorted(set(changed)),ensure_ascii=False,indent=2),encoding='utf-8')
info=dict(apk=dict(path=apkpath.name,bytes=apkpath.stat().st_size,sha256=sha(apkpath),versionCode=20,versionName='0.8.6',applicationId='dev.vincent.geschichten',signerSha256=signer,sameSignerAs085=True,v2Signed=True,zipalign16KiB=True,byteEqualNativeAndAssetFiles=len(shared),testDataAndNewWeightsNotPackaged=True),
          database=dict(primarySchema=10,semanticIndexSchema=1,changed=False),normalTests=normal,integrationTests=integration,overlappingTests=overlap,lint=lint,
          realInferences=counts,realInferenceCount=sum(counts.values()),ownNativeTokenChecks=token_checks,wholeReplyUseful=dict(A=2,B=2,C=2,cases=6,self=0,selfCases=2,selfFactualCoreCorrect=1),
          currentFinalControllerReplayCases=14,defaultTeamEnabled=False,defaultShortFactsEnabled=False,
          modelFiles=weights,catalogUnchanged=True,preserved085ArtifactFiles=len(old_art),preservedMemoryCore=preserved,
          modelSelectionSilentlyChanged=False,cloud=False,training=False,published=False,
          device='No ADB device; S24/S24 Ultra validation remains open',
          rawFiles=[dict(file=f.name,sha256=sha(f)) for f in p.glob('raw-*.json')],
          productionSourceHashes={f.relative_to(root).as_posix():sha(f) for f in (root/'app/src/main').rglob('*') if f.is_file()})
(p/'delivery-verification.json').write_text(json.dumps(info,ensure_ascii=False,indent=2),encoding='utf-8')
manifest=[dict(file=f.relative_to(p).as_posix(),sha256=sha(f)) for f in p.rglob('*') if f.is_file() and 'baseline-0.8.5' not in f.parts and f.name!='artifact-hashes.json']
(p/'artifact-hashes.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(dict(apk=info['apk'],normal=normal,integration=integration,overlap=overlap,lint=lint,oldArtifacts=len(old_art),modelFiles=len(weights),nativeTokenChecks=token_checks),ensure_ascii=False))
