"""Public pinned artifacts, hash checked; never overwrite existing model files."""
import hashlib,json,urllib.request
from pathlib import Path
ROOT=Path(__file__).resolve().parent
items=[('bartowski/Qwen_Qwen3-4B-Instruct-2507-GGUF','ae44f08e1392f39c0e474af10c3ff8355c8b6688','Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf',2497280736,'2fde00ce69dd4899c70d020845e2638353015bba0fdf161b3eb965f2bca4464e'),('ggml-org/embeddinggemma-300M-GGUF','0f741b5a6585bd53aeb15cd1372c56f2a0f65e12','embeddinggemma-300M-Q8_0.gguf',333590944,'b5ce9d77a3fc4b3b39ccb5643c36777911cc4eb46a66962eadfa3f5f60490d63')]
out=ROOT/'model-files';out.mkdir(exist_ok=True)
records=[]
for repo,rev,name,size,digest in items:
    meta=json.load(urllib.request.urlopen(f'https://huggingface.co/api/models/{repo}/revision/{rev}?blobs=true',timeout=30))
    (ROOT/(name+'.metadata.json')).write_text(json.dumps(meta,indent=2),encoding='utf-8')
    for small in ['README.md']:
        (ROOT/(name+'.'+small+'.txt')).write_bytes(urllib.request.urlopen(f'https://huggingface.co/{repo}/resolve/{rev}/{small}',timeout=30).read())
    dest=out/name
    if not dest.exists():
        tmp=out/(name+'.partial')
        h=hashlib.sha256();done=0
        with urllib.request.urlopen(f'https://huggingface.co/{repo}/resolve/{rev}/{name}?download=true',timeout=120) as response,tmp.open('wb') as f:
            while data:=response.read(8*1024*1024): f.write(data);h.update(data);done+=len(data)
        assert done==size and h.hexdigest()==digest,(done,h.hexdigest())
        tmp.rename(dest)
    h=hashlib.sha256()
    with dest.open('rb') as f:
        while d:=f.read(8*1024*1024):h.update(d)
    assert dest.stat().st_size==size and h.hexdigest()==digest
    records.append(dict(repository=repo,revision=rev,fileName=name,bytes=size,sha256=digest));print('Verified '+name,flush=True)
(ROOT/'download-verification.json').write_text(json.dumps(records,indent=2),encoding='utf-8')
