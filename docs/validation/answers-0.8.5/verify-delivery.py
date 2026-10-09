from pathlib import Path
import json, hashlib, zipfile, xml.etree.ElementTree as E

root = Path(__file__).resolve().parents[3]
p = Path(__file__).resolve().parent

def sha(f):
    h = hashlib.sha256()
    with f.open('rb') as stream:
        for b in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(b)
    return h.hexdigest()

prior = json.loads((root/'docs/validation/facts-0.8.4/delivery-verification.json').read_text())
weights = {}
for name, gold in prior['modelFiles'].items():
    paths = [root/'docs/validation/quality-search-0.8.3/model-files'/name,
             root/'docs/validation/models-0.7.0/model-files'/name]
    f = next(f for f in paths if f.exists())
    digest = sha(f)
    assert digest == gold['sha256']
    weights[name] = dict(size=f.stat().st_size, sha256=digest, unchanged=True)

old = json.loads((p/'baseline-hashes.json').read_text())
oldart = [r for r in old if r['path'].startswith('docs/validation/facts-0.8.4/')]
assert all(sha(root/r['path']) == r['sha256'] for r in oldart)

apkpath = root/'Geschichten-0.8.5.apk'
with zipfile.ZipFile(apkpath) as apk, zipfile.ZipFile(root/'Geschichten-0.8.4.apk') as oldapk:
    names = apk.namelist()
    # Existing GGUF tokenizer-only files are not neural model weights.
    assert not any('validation' in f or f.endswith('.litertlm') or
                   (f.endswith('.gguf') and not f.startswith('assets/tokenizers/')) for f in names)
    for f in names:
        if f.endswith('.dex'):
            assert b'conversation-frozen.json' not in apk.read(f) and b'new-holder' not in apk.read(f)
    shared = [f for f in oldapk.namelist() if f.startswith(('lib/', 'assets/'))]
    assert all(apk.read(f) == oldapk.read(f) for f in shared)
signer = '3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40'
signlog = (p/'apksigner.txt').read_text()
assert signer in signlog and 'Verified using v2 scheme (APK Signature Scheme v2): true' in signlog
assert (p/'zipalign.txt').read_text().strip().endswith('Verification succesful')
assert "versionCode='19' versionName='0.8.5'" in (p/'apk-badging.txt').read_text()

changed = []
for r in old:
    if r['path'].startswith('app/src/') or r['path'] in ['app/build.gradle.kts', 'README.md', 'BUILD_WINDOWS.cmd']:
        if sha(root/r['path']) != r['sha256']:
            changed.append(r['path'])
known = {r['path'] for r in old}
changed += [f.relative_to(root).as_posix() for f in (root/'app/src').rglob('*')
            if f.is_file() and f.relative_to(root).as_posix() not in known]
changed += ['docs/PRUEFBERICHT-0.8.5.md']
(p/'changed-files.json').write_text(json.dumps(changed, indent=2), encoding='utf-8')

def tests(folder):
    rows = [E.parse(f).getroot() for f in (p/folder).glob('TEST-*.xml')]
    return {k: sum(int(r.attrib[k]) for r in rows) for k in ['tests', 'failures', 'errors', 'skipped']}

rawhashes = [dict(file=f.name, sha256=sha(f)) for f in p.glob('raw-*.json')]
info = dict(
    apk=dict(path='Geschichten-0.8.5.apk', bytes=apkpath.stat().st_size, sha256=sha(apkpath),
             applicationId='dev.vincent.geschichten', versionName='0.8.5', versionCode=19,
             signerSha256=signer, sameSignerAs084=True, v2Signed=True, zipalign16KiB=True,
             modelAssetsAndNativeFilesByteEqualTo084=len(shared), testCasesAndModelWeightsNotPackaged=True),
    database=dict(primarySchema=10, semanticIndexSchema=1, schemaChanged=False),
    normalTests=tests('normal-results'), integrationTests=tests('integration-results'), overlappingTests=18,
    extraTargetedNativeTests='NativeSemanticFlowTest and NativeConversationPersistenceTest passed separately; logs retained',
    lint=dict(errors=0, warnings=132, information=1, addedWarning='UseKtx for checked synchronous preferences commit'),
    preserved084ArtifactFiles=len(oldart), modelFiles=weights, modelSelectionChanged=False,
    published=False, training=False, cloudInference=False, realGenerationCount=30,
    validPairedGenerationCount=18, excludedDiagnosticGenerationCount=6,
    independentNewAcceptance=dict(positive=5, negativeOrAmbiguous=13, development=2,
                                  correctPositiveAnswers=5, falseActivations=0,
                                  bothFreeAndFactsCorrectSimpleQuestions=5, defaultFactsModeEnabled=False),
    actualTokenBudgetChecks=30, nativeSemanticEnabledCases=6, nativeSemanticFallbacks=0,
    wholeUsefulComparison=dict(lexical=1, hybrid=1, hybridWithContext=3, caseCount=6),
    deviceCheck='No attached ADB device; S24/S24 Ultra open', rawFiles=rawhashes,
    productionSourceHashes={f.relative_to(root).as_posix(): sha(f)
                            for f in (root/'app/src/main').rglob('*') if f.is_file()})
(p/'delivery-verification.json').write_text(json.dumps(info, ensure_ascii=False, indent=2), encoding='utf-8')
manifest = [dict(file=f.relative_to(p).as_posix(), sha256=sha(f)) for f in p.rglob('*')
            if f.is_file() and not any(x in f.parts for x in ['baseline-0.8.4', 'final-classes'])
            and f.name != 'artifact-hashes.json']
(p/'artifact-hashes.json').write_text(json.dumps(manifest, indent=2), encoding='utf-8')
print('Verified', info['apk'])
print('Model hashes unchanged', len(weights), 'old artifacts unchanged', len(oldart))
