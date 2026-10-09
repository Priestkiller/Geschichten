"""Read the official GGUF v3 metadata and tensor headers, no optional dependencies."""
import struct,json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
FORMATS={0:'B',1:'b',2:'H',3:'h',4:'I',5:'i',6:'f',7:'?',10:'Q',11:'q',12:'d'}
def read_model(path):
    with path.open('rb') as f:
        def number(fmt):return struct.unpack('<'+fmt,f.read(struct.calcsize(fmt)))[0]
        def string():return f.read(number('Q')).decode('utf-8')
        def value(kind):
            if kind==8:return string()
            if kind==9:
                sub=number('I');n=number('Q');return [value(sub) for _ in range(n)]
            return number(FORMATS[kind])
        assert f.read(4)==b'GGUF' and number('I')==3
        tensors=number('Q');count=number('Q');fields={}
        for _ in range(count):
            key=string();v=value(number('I'))
            if key.startswith('general.') or any(s in key for s in ('chat_template','bos_token_id','eos_token_id','add_bos','add_eos','pooling','dense','context_length','embedding_length')):fields[key]=v
        dense=[]
        for _ in range(tensors):
            name=string();shape=[number('Q') for _ in range(number('I'))];kind=number('I');offset=number('Q')
            if 'dense' in name:dense.append(dict(name=name,shape=shape,type=kind))
        return dict(file=path.name,fields=fields,denseTensors=dense)
result=[read_model(ROOT/'model-files'/n) for n in ['Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf','embeddinggemma-300M-Q8_0.gguf']]
(ROOT/'gguf-metadata.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
for x in result:print(x['file'],x['fields'].get('general.architecture'),x['denseTensors'])
