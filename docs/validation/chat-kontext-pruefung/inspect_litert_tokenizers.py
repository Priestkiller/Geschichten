"""Reads the pinned LiteRT-LM header (official v0.17.1 FlatBuffer schema) and tokenizer sections."""
import json,pathlib,struct,zlib
import sentencepiece as spm
from tokenizers import Tokenizer
ROOT=pathlib.Path(__file__).resolve().parents[3]
AUDIT=ROOT/'docs/validation/chat-kontext-pruefung'
FILES=ROOT/'docs/validation/models-0.7.0/model-files'
def u(data,pos,fmt):return struct.unpack_from('<'+fmt,data,pos)[0]
def field(data,pos,index):
    vt=pos-u(data,pos,'i'); offset=4+index*2
    return pos+u(data,vt+offset,'H') if offset<u(data,vt,'H') and u(data,vt+offset,'H') else 0
def indirect(data,pos):return pos+u(data,pos,'I')
def vec(data,pos):
    if not pos:return []
    pos=indirect(data,pos);return [indirect(data,pos+4+4*i) for i in range(u(data,pos,'I'))]
def string(data,pos):
    pos=indirect(data,pos);return data[pos+4:pos+4+u(data,pos,'I')].decode('utf-8')
def kv(data,pos):
    key=string(data,field(data,pos,0)); kind=u(data,field(data,pos,1),'B'); value=indirect(data,field(data,pos,2)); p=field(data,value,0)
    fmts={1:'B',2:'b',3:'H',4:'h',5:'I',6:'i',7:'f',8:'?',10:'Q',11:'q',12:'d'}
    return key,string(data,p) if kind==9 else u(data,p,fmts[kind]) if p else 0
def varint(data,pos):
    value=0;shift=0
    while True:
        byte=data[pos];pos+=1;value|=(byte&127)<<shift
        if byte<128:return value,pos
        shift+=7
def protobuf(data):
    result={};pos=0
    while pos<len(data):
        key,pos=varint(data,pos);kind=key&7;number=key>>3
        if kind==0:value,pos=varint(data,pos)
        elif kind==2:
            size,pos=varint(data,pos);value=data[pos:pos+size];pos+=size
        elif kind==5:value=data[pos:pos+4];pos+=4
        elif kind==1:value=data[pos:pos+8];pos+=8
        else:raise ValueError(kind)
        result.setdefault(number,[]).append(value)
    return result
def token_union(data):
    fields=protobuf(data)
    if 2 in fields:return {'text':fields[2][0].decode('utf-8')}
    packed=protobuf(fields[1][0]).get(1,[]);ids=[]
    for values in packed:
        if isinstance(values,int):ids.append(values);continue
        pos=0
        while pos<len(values):value,pos=varint(values,pos);ids.append(value)
    return {'ids':ids}
out=[]
for name,tag in [('gemma-4-E2B-it.litertlm','gemma4'),('Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm','qwen25'),('Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm','qwen06')]:
    with (FILES/name).open('rb') as f:
        head=f.read(32);assert head[:8]==b'LITERTLM';end=u(head,24,'Q');f.seek(32);data=f.read(end-32)
        root=u(data,0,'I');section_meta=indirect(data,field(data,root,1));sections=[];encode=None;llm={}
        for pos in vec(data,field(data,section_meta,0)):
            begin=u(data,field(data,pos,1),'Q');finish=u(data,field(data,pos,2),'Q');kind=u(data,field(data,pos,3),'B')
            metadata=dict(kv(data,x) for x in vec(data,field(data,pos,0)))
            sections.append({'type':kind,'begin':begin,'end':finish,'metadata':metadata})
            if kind==5:
                f.seek(begin);meta=protobuf(f.read(finish-begin))
                llm={'start':token_union(meta[1][0]) if 1 in meta else None,
                     'stops':[token_union(x) for x in meta.get(2,[])],
                     'maxNumTokensMetadata':meta.get(5,[0])[0],
                     'jinjaTemplate':meta[7][0].decode('utf-8') if 7 in meta else None,
                     'supportsThinking':meta.get(11,[0])[0]}
            if kind in (4,6):
                f.seek(begin);raw=f.read(finish-begin)
                if kind==4:
                    tokenizer=spm.SentencePieceProcessor(model_proto=raw)
                    encode=lambda text,t=tokenizer:t.encode(text,out_type=int)
                    tokenizer_kind='SentencePiece';special={'bos':tokenizer.bos_id(),'eos':tokenizer.eos_id()}
                else:
                    # HF tokenizer section: uint64 uncompressed size followed by the zlib stream.
                    decoded_raw=zlib.decompress(raw[8:]);assert len(decoded_raw)==u(raw,0,'Q')
                    decoded=decoded_raw.decode('utf-8');tokenizer=Tokenizer.from_str(decoded)
                    encode=lambda text,t=tokenizer:t.encode(text,add_special_tokens=False).ids
                    tokenizer_kind='embedded HF tokenizer JSON (zlib)';special={}
        assert encode is not None
    rows=[]
    source=AUDIT/(tag+'-tokens.json')
    if source.exists():
        for native in json.loads(source.read_text(encoding='utf-8')):
            if 'error' in native:continue
            full=native['currentRendered']; ids=encode(full)
            start=llm['start'];start_ids=(start.get('ids',[]) if 'ids' in start else encode(start['text'])) if start else []
            # Isolated pieces are explanatory counts, not an additive allocation guarantee.
            sources=json.loads((AUDIT/'inputs-with-overflow.json').read_text(encoding='utf-8'))
            inp=next(x for x in sources if x['case']==native['case'])
            system=inp['system'];pieces=system.split('\n\n');parts={}
            for i,piece in enumerate(pieces):parts['rules' if i==0 else piece.split(':\n')[0]]=len(encode(piece))
            rows.append({'case':native['case'],'renderedTokensWithoutAutomaticSpecials':len(ids),'nativePrefillTokens':native['prefillTokens'],
                         'difference':native['prefillTokens']-len(ids),'firstTokenIds':ids[:8],'lastTokenIds':ids[-8:],
                         'automaticStartTokenIds':start_ids,'countIncludingStart':len(ids)+len(start_ids),
                         'exactlyMatchesNative':len(ids)+len(start_ids)==native['prefillTokens'],
                         'isolatedSystemPartsNotAdditive':parts,'systemContentTokens':len(encode(system)),
                         'historyContentTokens':sum(len(encode(m['text'])) for m in inp['history'][:-1]),'latestUserContentTokens':len(encode(inp['history'][-1]['text']))})
    out.append({'file':name,'tokenizer':tokenizer_kind,'specials':special,'llmMetadata':llm,'sections':sections,'counts':rows})
    (AUDIT/'litert-tokenizers.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
    print(name,tokenizer_kind,[(r['case'],r['nativePrefillTokens'],r['difference']) for r in rows])
