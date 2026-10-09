"""Inspect this local test APK and summarize independent deterministic/model records."""
from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET

out=Path(__file__).resolve().parent
root=out.parents[2]
def sha(data):return hashlib.sha256(data).hexdigest()
def save(name,data):(out/name).write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding="utf-8")
def tests(folder):
    totals=dict(tests=0,failures=0,errors=0,skipped=0)
    for file in folder.glob("TEST-*.xml"):
        tree=ET.parse(file).getroot()
        for key in totals:totals[key]+=int(tree.get(key,"0"))
    return totals
lint=ET.parse(out/"lint-final.xml").getroot()
save("test-summary.json",dict(normal=tests(out/"unit-test-xml"),targeted=tests(out/"targeted-test-xml"),lint={severity:sum(i.get("severity")==severity for i in lint.findall("issue")) for severity in ["Error","Warning","Information"]},note="Overlapping runs, not added; actual model answers are a separate layer."))
apk=root/"Geschichten-0.8.1.apk"
prior=json.loads((out.parent/"dauerhaftes-gedaechtnis/apk-verification.json").read_text(encoding="utf-8"))
with zipfile.ZipFile(apk) as archive:
    for lib in prior["libraries"]:assert sha(archive.read(lib["path"]))==lib["sha256"]
    for asset in prior["tokenizerAssets"]:assert sha(archive.read(asset["path"]))==asset["sha256"]
    dex=b"".join(archive.read(n) for n in archive.namelist() if n.endswith(".dex"))
    probes=["MemoryModelProbe","ArchiveRecallTest","ActiveMemoryModelFixturesTest","MemoryRepositoryIntegrationTest","org/robolectric"]
    assert all(p.encode() not in dex for p in probes)
signature=(out/"apk-signature.txt").read_text(encoding="utf-8-sig")
badging=(out/"apk-badging.txt").read_text(encoding="utf-8-sig")
assert prior["signerSha256"] in signature and "v2 scheme (APK Signature Scheme v2): true" in signature
assert "versionCode='15' versionName='0.8.1'" in badging
assert "Verification succesful" in (out/"apk-alignment.txt").read_text(encoding="utf-8-sig")
save("apk-verification.json",dict(file=str(apk),bytes=apk.stat().st_size,sha256=sha(apk.read_bytes()),version="0.8.1",versionCode=15,database=9,package="dev.vincent.geschichten",signerSha256=prior["signerSha256"],signingV2=True,diagnosticsAbsent=True,nativeAndTokenizerDataIdenticalTo080=True,alignment16k=True,published=False))
baseline=json.loads((out.parent/"dauerhaftes-gedaechtnis/source-changes.json").read_text(encoding="utf-8"))
before={r["path"]:r["afterSha256"] for r in baseline["records"] if r["afterSha256"]}
paths=set(before)
paths.update(f.relative_to(root).as_posix() for f in (root/"app/src").rglob("*") if f.is_file())
paths.update(["docs/aktives-gedaechtnis-0.8.1.md","docs/PRUEFBERICHT-0.8.1.md"])
changed=[]
for name in sorted(paths):
    file=root/name
    after=sha(file.read_bytes()) if file.is_file() else None
    if before.get(name)!=after:changed.append(dict(path=name,beforeSha256=before.get(name),afterSha256=after))
assert all(r["afterSha256"] for r in changed)
save("source-changes.json",dict(baseline="0.8.0 source-changes.json after hashes",changed=changed,removed=0))
files=sorted((out/"models-final").glob("*-answers.json"))
rows=[row for file in files for row in json.loads(file.read_text(encoding="utf-8"))]
if len(files)==6 and len(rows)==18:
    assert all(r["plannedInputTokens"]==r["nativePromptTokens"] and r["nativePromptTokens"]+512<=4096 for r in rows)
    for r in rows:
        if r["case"]=="archive":
            assert r["recalledSources"] and "Losungswort am Brunnen lautet Morgenstern." in r["system"] and "Siegel nicht unbeschädigt öffnen" in r["system"]
        if r["case"]=="correction":assert "Farbe: blau" in r["system"]
        if r["case"]=="unknown":assert not r["recalledSources"]
    save("model-handoff-summary.json",dict(calls=18,modelCount=6,allPromptCountsMatch=True,allInputsWithinBudget=True,archiveOriginalsActuallyPresent=True,immediateBlueCorrectionActuallyPresent=True,unknownEventNotRecalled=True,inputsSha256=sha((out/"model-cases-final.json").read_bytes()),additionalMemoryModelCalls=0,models=[dict(model=r["model"],case=r["case"],tokens=r["nativePromptTokens"],planningMs=r["planningMs"],accepted=r["accepted"],error=r.get("error")) for r in rows],note="Acceptance is not a correctness verdict; read the separate manual model assessment."))
print(json.dumps(dict(tests=json.loads((out/"test-summary.json").read_text(encoding="utf-8")),apk=json.loads((out/"apk-verification.json").read_text(encoding="utf-8")),modelCallsRecorded=len(rows)),ensure_ascii=False,indent=2))
