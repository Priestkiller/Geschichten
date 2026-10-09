import ctypes,os,json,time,math,hashlib,sys
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parent
PROJECT=ROOT.parents[2]
compiler=PROJECT.parent/'.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin'
os.add_dll_directory(str(compiler));os.add_dll_directory(str(ROOT/'native-cpu'))
dll=ctypes.CDLL(str(ROOT/'native-cpu/geschichten_gguf.dll'))
dll.embedding_probe_create.argtypes=[ctypes.c_char_p];dll.embedding_probe_create.restype=ctypes.c_void_p
dll.embedding_probe_encode.argtypes=[ctypes.c_void_p,ctypes.c_char_p,ctypes.POINTER(ctypes.c_float)];dll.embedding_probe_encode.restype=ctypes.c_int
dll.embedding_probe_destroy.argtypes=[ctypes.c_void_p]
t=time.perf_counter();handle=dll.embedding_probe_create(str(ROOT/'model-files/embeddinggemma-300M-Q8_0.gguf').encode())
assert handle,'Embedding model load failed'
load_ms=(time.perf_counter()-t)*1000
def encode(s):
    v=(ctypes.c_float*768)();start=time.perf_counter();n=dll.embedding_probe_encode(handle,s.encode(),v)
    assert n==768,(n,s[:100]);return list(v),(time.perf_counter()-start)*1000
try:
    docs=['Alva bekommt in engen, dunklen Räumen schnell Panik.','Henrik ist höhenängstlich.','Der unterirdische Gang ist schmal, seine Wände sind aus Kalkstein.','Alva liebt weite offene Landschaften.','Henrik repariert ein Gerät.']
    question='Wie reagierst du auf den schmalen unterirdischen Gang?'
    vectors=[];times=[]
    for text in docs:
        v,ms=encode('title: none | text: '+text);vectors.append(v);times.append(ms)
    q,qms=encode('task: search result | query: '+question)
    scores=[float(np.dot(v,q)) for v in vectors]
    report=dict(model='ggml-org/embeddinggemma-300M-GGUF',revision='0f741b5a6585bd53aeb15cd1372c56f2a0f65e12',dimension=768,pooling='mean + model dense projections',normalization='L2',context=512,maxDocumentChars=1200,queryPrefix='task: search result | query: ',documentPrefix='title: none | text: ',loadMs=load_ms,queryMs=qms,documents=[dict(text=t,score=s,ms=ms,norm=math.sqrt(sum(x*x for x in v))) for t,s,ms,v in zip(docs,scores,times,vectors)],question=question,queryVector=q,documentVectors=vectors)
    (ROOT/'embedding-pilot.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({k:v for k,v in report.items() if k not in ('queryVector','documentVectors')},ensure_ascii=False),flush=True)
finally:dll.embedding_probe_destroy(handle)
