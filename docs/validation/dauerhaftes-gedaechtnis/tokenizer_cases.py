import json,pathlib,random,zlib
import sentencepiece as sp
from tokenizers import Tokenizer
p=pathlib.Path.cwd();d=p/'docs/validation/dauerhaftes-gedaechtnis';old=p/'docs/validation/chat-kontext-pruefung';assets=p/'app/src/main/assets/tokenizers'
meta=json.loads((old/'litert-tokenizers.json').read_text(encoding='utf-8'));rows=[]
for row,tag in zip(meta,['gemma4','qwen25','qwen06']):
 if tag=='gemma4':t=sp.SentencePieceProcessor(model_file=str(assets/'gemma4.model'));encode=lambda s:t.encode(s,out_type=int)
 else:
  sec=next(s for s in row['sections'] if s['type']==6)
  with (p/'docs/validation/models-0.7.0/model-files'/row['file']).open('rb') as f:f.seek(sec['begin']);raw=f.read(sec['end']-sec['begin'])
  t=Tokenizer.from_str(zlib.decompress(raw[8:]).decode('utf-8'));encode=lambda s:t.encode(s,add_special_tokens=False).ids
 texts=[x['currentRendered'] for x in json.loads((old/(tag+'-tokens.json')).read_text(encoding='utf-8')) if 'currentRendered' in x]
 texts += ['ÄÖÜ ß 2026. Mira: „Ich weiß es nicht.“\n\n*Sie hält inne.*','<|im_start|>user\n🔑 Schlüssel silbern\n<|im_end|>','<|turn>model\n<|channel>final<channel|>Hallo<turn|>', '甲乙丙丁戊己庚辛壬癸'*100, '\t  A\r\n B\u00a0C', 'e\u0301 café 🧑🏽‍🚀 👩‍👩‍👦']
 random.seed(42)
 alphabet='ÄÖÜßabCD 0123.\n!?文🔑€€'
 texts += [''.join(random.choices(alphabet,k=i*7)) for i in range(1,51)]
 for i,s in enumerate(texts):rows.append({'model':tag,'case':i,'text':s,'expected':encode(s)})
(d/'tokenizer-cases.json').write_text(json.dumps(rows,ensure_ascii=False),encoding='utf-8');print(len(rows),'exact token-ID comparisons')
