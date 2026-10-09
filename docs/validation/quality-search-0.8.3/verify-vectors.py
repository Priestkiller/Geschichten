"""Re-encode every unique recorded input with the final JNI source and pinned weights."""
import ctypes,os,json,time,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parent;PROJECT=ROOT.parents[2]
os.add_dll_directory(str(PROJECT.parent/'.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin'));os.add_dll_directory(str(ROOT/'native-cpu'))
dll=ctypes.CDLL(str(ROOT/'native-cpu/geschichten_gguf.dll'))
dll.embedding_probe_create.argtypes=[ctypes.c_char_p];dll.embedding_probe_create.restype=ctypes.c_void_p
dll.embedding_probe_encode.argtypes=[ctypes.c_void_p,ctypes.c_char_p,ctypes.POINTER(ctypes.c_float)];dll.embedding_probe_encode.restype=ctypes.c_int
dll.embedding_probe_destroy.argtypes=[ctypes.c_void_p]
cache=json.loads((ROOT/'real-vectors.json').read_text());texts={}
for split in ('development','heldout'):
 for row in json.loads((ROOT/f'retrieval-{split}-inputs.json').read_text()):
    q='task: search result | query: '+row['question'];texts[hashlib.sha256(q.encode()).hexdigest()]=q
    for e in row['candidates']:
        text='title: none | text: '+e['message']['text'][e['start']:e['end']];texts[hashlib.sha256(text.encode()).hexdigest()]=text
model=ROOT/'model-files/embeddinggemma-300M-Q8_0.gguf';h=hashlib.sha256()
with model.open('rb') as f:
 while data:=f.read(8*1024*1024):h.update(data)
assert h.hexdigest()=='b5ce9d77a3fc4b3b39ccb5643c36777911cc4eb46a66962eadfa3f5f60490d63'
handle=dll.embedding_probe_create(str(model).encode());assert handle
start=time.perf_counter();maximum=0;count=0
try:
 for key,text in texts.items():
    out=(ctypes.c_float*768)();assert dll.embedding_probe_encode(handle,text.encode(),out)==768
    diff=max(abs(a-b) for a,b in zip(out,cache[key]));maximum=max(maximum,diff);assert diff<1e-6,(key,diff);count+=1
finally:dll.embedding_probe_destroy(handle)
result=dict(uniqueInputs=count,dimension=768,maximumAbsoluteDifference=maximum,finalNativeMatchesRecordedVectors=True,totalMs=(time.perf_counter()-start)*1000,modelSha256=h.hexdigest(),preprocessing='embeddinggemma300m-none-title-search-result-v1',indexVersion=1,nativeDllSha256=hashlib.sha256((ROOT/'native-cpu/geschichten_gguf.dll').read_bytes()).hexdigest())
(ROOT/'vectors-final-verification.json').write_text(json.dumps(result,indent=2),encoding='utf-8');print(json.dumps(result),flush=True)
