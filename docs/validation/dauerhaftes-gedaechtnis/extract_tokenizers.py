import json,pathlib,zlib,hashlib
from gguf import GGUFWriter
p=pathlib.Path.cwd(); d=p/'docs/validation/dauerhaftes-gedaechtnis'; a=p/'app/src/main/assets/tokenizers';a.mkdir(parents=True,exist_ok=True)
models=json.loads((p/'docs/validation/chat-kontext-pruefung/litert-tokenizers.json').read_text(encoding='utf-8'));out=[]
for row in models:
 sec=next(s for s in row['sections'] if s['type'] in (4,6))
 with (p/'docs/validation/models-0.7.0/model-files'/row['file']).open('rb') as f:f.seek(sec['begin']);raw=f.read(sec['end']-sec['begin'])
 if sec['type']==4:name='gemma4.model';(a/name).write_bytes(raw)
 else:
  data=json.loads(zlib.decompress(raw[8:]));name='qwen25.gguf' if 'Qwen2.5' in row['file'] else 'qwen06.gguf'
  vocab=data['model']['vocab'];added=data['added_tokens'];n=max(max(vocab.values()),max(t['id'] for t in added))+1
  tokens=['[PAD%d]'%i for i in range(n)];types=[1]*n
  for token,index in vocab.items():tokens[index]=token
  for t in added:tokens[t['id']]=t['content'];types[t['id']]=3 if t['special'] else 4
  w=GGUFWriter(a/name,'qwen2');w.add_tokenizer_model('gpt2');w.add_tokenizer_pre('qwen2');w.add_token_list(tokens);w.add_token_types(types)
  w.add_token_merges([' '.join(x) if isinstance(x,list) else x for x in data['model']['merges']]);w.add_add_bos_token(False);w.add_add_eos_token(False);w.add_eos_token_id(vocab.get('<|im_end|>',151645))
  w.write_header_to_file();w.write_kv_data_to_file();w.write_tensors_to_file();w.close()
 out.append({'file':name,'source':row['file'],'sha256':hashlib.sha256((a/name).read_bytes()).hexdigest(),'bytes':(a/name).stat().st_size,'sourceSectionSha256':hashlib.sha256(raw).hexdigest()})
(d/'tokenizer-assets.json').write_text(json.dumps(out,indent=2),encoding='utf-8');print(out)
