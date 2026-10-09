import json,hashlib,zipfile,xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parent;PROJECT=ROOT.parents[2]
def sha(p):
 h=hashlib.sha256()
 with p.open('rb') as f:
  while d:=f.read(8*1024*1024):h.update(d)
 return h.hexdigest()
baseline=json.loads((ROOT/'baseline.json').read_text());old=PROJECT/'docs/validation/roles-0.8.2'
immutable=[];diagnostic_changes=[]
for name,h in baseline['immutable082'].items():
 p=old/name;actual=sha(p)
 if actual!=h:diagnostic_changes.append(dict(file=name,beforeSha256=h,afterSha256=actual))
 if name=='controlled-inputs.json' or name.startswith('production-final/') or 'comparison.json' in name or name.startswith('automatic-second/') and name.endswith('answers.json'):
  assert actual==h,name;immutable.append(dict(file=name,sha256=h))
apk=PROJECT/'Geschichten-0.8.3.apk';old_apk=PROJECT/'Geschichten-0.8.2.apk'
unchanged=[];new_native=[];no_models=True
with zipfile.ZipFile(old_apk) as before,zipfile.ZipFile(apk) as after:
 for name in before.namelist():
  if name.startswith(('assets/','res/drawable')) or name.startswith('lib/') and name.endswith('.so'):
   if name not in after.namelist():continue
   a=hashlib.sha256(before.read(name)).hexdigest();b=hashlib.sha256(after.read(name)).hexdigest()
   if a==b:unchanged.append(dict(entry=name,sha256=b))
   elif name.startswith('lib/'):new_native.append(dict(entry=name,beforeSha256=a,afterSha256=b))
 # Existing vocabulary-only GGUF tokenizer assets are not model weights.
 for n in after.namelist():
  if n.endswith(('.gguf','.litertlm')):
   assert n.startswith('assets/tokenizers/') and n in before.namelist(),n
   assert hashlib.sha256(before.read(n)).digest()==hashlib.sha256(after.read(n)).digest(),n
 for n in after.namelist():
  if n.endswith('.dex'):
   data=after.read(n)
   for forbidden in [b'QualityProbe',b'ProductionSemanticVerificationTest',b'NewScenePersistenceAuditTest']:assert forbidden not in data,(n,forbidden)
 native=after.read('lib/arm64-v8a/libgeschichten_gguf.so')
 assert b'Java_dev_vincent_geschichten_ai_EmbeddingNative_encode' in native
 assert b'Java_dev_vincent_geschichten_ai_QualityProbe_setSampling' not in native
database='dev/vincent/geschichten/data/StoryRepository$Database.class'
assert sha(ROOT/'baseline-classes'/database)==sha(PROJECT/'app/build/tmp/kotlin-classes/debug'/database)
changed=[];added=[]
for p in (PROJECT/'app/src').rglob('*'):
 if not p.is_file() or p.suffix not in ('.kt','.cpp','.h','.txt'):continue
 relative=p.relative_to(PROJECT);prior=ROOT/'baseline-source'/relative
 current=sha(p)
 if not prior.exists():added.append(dict(path=str(relative),sha256=current))
 elif sha(prior)!=current:changed.append(dict(path=str(relative),beforeSha256=sha(prior),afterSha256=current))
for name in ['app/build.gradle.kts','BUILD_WINDOWS.cmd','build-linux.sh','README.md']:
 before=next(x['sha256'] for x in baseline['sourceChecks'] if x['path']==name)
 changed.append(dict(path=name,beforeSha256=before,afterSha256=sha(PROJECT/name)))
catalog='app/src/main/java/dev/vincent/geschichten/ai/LocalModelCatalog.kt'
old_catalog=(ROOT/'baseline-source'/catalog).read_text();new_catalog=(PROJECT/catalog).read_text()
begin=new_catalog.index('        LocalModelSpec(\n            id = "qwen3-official-4b"')
end=new_catalog.index('\n        ),',begin)+len('\n        ),\n')
assert new_catalog[:begin]+new_catalog[end:]==old_catalog,'Existing model specifications changed'
tests={}
for directory in ('normal-results','integration-results'):
 roots=[ET.parse(p).getroot() for p in (ROOT/directory).glob('TEST-*.xml')]
 tests[directory]=dict(tests=sum(int(r.attrib['tests']) for r in roots),failures=sum(int(r.attrib['failures']) for r in roots),errors=sum(int(r.attrib['errors']) for r in roots),classes=[r.attrib['name'] for r in roots])
lint=ET.parse(PROJECT/'app/build/reports/lint-results-debug.xml').getroot()
severities={}
for issue in lint.findall('issue'):severities[issue.attrib['severity']]=severities.get(issue.attrib['severity'],0)+1
legacy=[]
for f in baseline['models']:
 p=PROJECT/'docs/validation/models-0.7.0/model-files'/f['file'];actual=sha(p);assert actual==f['sha256'];legacy.append(dict(file=f['file'],sha256=actual,unchanged=True))
result=dict(apk=dict(file=str(apk),bytes=apk.stat().st_size,sha256=sha(apk),package='dev.vincent.geschichten',versionName='0.8.3',versionCode=17,signerSha256='3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40'),databaseVersion=9,databaseClassUnchanged=True,databaseClassSha256=sha(ROOT/'baseline-classes'/database),existingSixModelSpecificationsUnchanged=True,unchangedApkEntries=unchanged,changedNativeEntries=new_native,noModelWeightsOrProbeClassesInApk=True,original082InferenceDataUnchanged=immutable,earlyDiagnosticReplaysChanged082Files=diagnostic_changes,existingModelFiles=legacy,changedSource=changed,addedSource=added,tests=tests,lint=severities)
(ROOT/'delivery-verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k in ('apk','databaseVersion','databaseClassUnchanged','tests','lint','earlyDiagnosticReplaysChanged082Files')},ensure_ascii=False,indent=2))
