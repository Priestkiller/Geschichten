"""Real pinned local embeddings; development thresholds only, then frozen held-out run."""
import ctypes,os,json,time,hashlib,sys
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parent;PROJECT=ROOT.parents[2]
split=sys.argv[1] if len(sys.argv)>1 else 'development'
os.add_dll_directory(str(PROJECT.parent/'.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin'));os.add_dll_directory(str(ROOT/'native-cpu'))
dll=ctypes.CDLL(str(ROOT/'native-cpu/geschichten_gguf.dll'))
dll.embedding_probe_create.argtypes=[ctypes.c_char_p];dll.embedding_probe_create.restype=ctypes.c_void_p
dll.embedding_probe_encode.argtypes=[ctypes.c_void_p,ctypes.c_char_p,ctypes.POINTER(ctypes.c_float)];dll.embedding_probe_encode.restype=ctypes.c_int
dll.embedding_probe_destroy.argtypes=[ctypes.c_void_p]
cachefile=ROOT/'real-vectors.json';cache=json.loads(cachefile.read_text()) if cachefile.exists() else {}
handle=dll.embedding_probe_create(str(ROOT/'model-files/embeddinggemma-300M-Q8_0.gguf').encode());assert handle
def encode(text):
    key=hashlib.sha256(text.encode()).hexdigest()
    if key in cache:return cache[key],0,False
    v=(ctypes.c_float*768)();t=time.perf_counter();assert dll.embedding_probe_encode(handle,text.encode(),v)==768
    ms=(time.perf_counter()-t)*1000;cache[key]=list(v);return list(v),ms,True
def key(e):return f"{e['message']['id']}:{e['start']}:{e['end']}"
def text(e):return e['message']['text'][e['start']:e['end']]
def fuse(lexical,semantic,threshold,limit=12):
    ranks={};objects={}
    for sequence in [lexical,[x['excerpt'] for x in semantic if x['cosine']>=threshold]]:
        for rank,e in enumerate(sequence[:32],1):
            k=key(e);ranks[k]=ranks.get(k,0)+1/(60+rank);objects[k]=e
    return [objects[k] for k in sorted(ranks,key=lambda k:-ranks[k])[:limit]]
inputs=json.loads((ROOT/f'retrieval-{split}-inputs.json').read_text())
thresholds=[.25,.30,.35] if split=='development' else [json.loads((ROOT/'parameters-frozen.json').read_text())['semanticThreshold']]
results=[]
try:
    for row in inputs:
        start=time.perf_counter();q,qms,_=encode('task: search result | query: '+row['question']);sem=[];indexms=0;new=0
        for e in row['candidates']:
            v,ms,created=encode('title: none | text: '+text(e));indexms+=ms;new+=created;sem.append(dict(excerpt=e,cosine=float(np.dot(q,v))))
        sem.sort(key=lambda x:-x['cosine'])
        expected=set(row['relevantSourceIds']);lex4=row['lexical'][:4]
        variants=[]
        for threshold in thresholds:
            hybrid=fuse(row['lexical'],sem,threshold)
            selected=hybrid[:4];found=expected.intersection(e['message']['id'] for e in selected)
            variants.append(dict(threshold=threshold,selected=selected,candidates=hybrid,relevantIdsSelected=sorted(found),allRequiredSelected=expected<=found,irrelevantSelected=sum(e['message']['id'] not in expected for e in selected)))
        results.append(dict(case=row['case'],expectedIds=sorted(expected),lexical=row['lexical'],lexicalRequiredSelected=sorted(expected.intersection(e['message']['id'] for e in lex4)),lexicalAllRequiredSelected=expected<=set(e['message']['id'] for e in lex4),lexicalMs=row['lexicalMs'],semantic=sem[:12],variants=variants,queryMs=qms,newVectorCount=new,indexMs=indexms,candidateCount=len(sem),storedVectorBytes=len(sem)*768*4,totalMs=(time.perf_counter()-start)*1000))
        print(row['case'],'lex',results[-1]['lexicalAllRequiredSelected'],[(v['threshold'],v['allRequiredSelected']) for v in variants],flush=True)
    (ROOT/f'retrieval-{split}-results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
finally:
    cachefile.write_text(json.dumps(cache),encoding='utf-8');dll.embedding_probe_destroy(handle)
