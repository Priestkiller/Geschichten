"""No training/merge/export. Same synthetic prompts and sampling with the saved adapter enabled/disabled."""
import os,pathlib,json,time,hashlib
ROOT=pathlib.Path(__file__).resolve().parents[3]
AUDIT=ROOT/'docs/validation/chat-kontext-pruefung'
PILOT=ROOT.parent/'training/geschichten-pilot-v1'
ADAPTER=PILOT/'runs/geschichten-qwen3-4b-v1/adapter'
for key,value in {'HF_HUB_OFFLINE':'1','TRANSFORMERS_OFFLINE':'1','HF_HOME':str(AUDIT/'hf-cache'),
 'UNSLOTH_COMPILE_LOCATION':str(AUDIT/'unsloth-cache'),'TORCHINDUCTOR_CACHE_DIR':str(AUDIT/'torch-cache'),
 'TRITON_CACHE_DIR':str(AUDIT/'triton-cache')}.items():os.environ[key]=value
from unsloth import FastLanguageModel
import torch
from transformers import set_seed
torch.set_num_threads(4)
model,tokenizer=FastLanguageModel.from_pretrained(model_name=str(ADAPTER),max_seq_length=4096,dtype=torch.bfloat16,
 load_in_4bit=True,local_files_only=True,trust_remote_code=False)
tokenizer.chat_template=(PILOT/'qwen-official-chat-template.jinja').read_text(encoding='utf-8')
FastLanguageModel.for_inference(model)
cases=json.loads((AUDIT/'inputs-after.json').read_text(encoding='utf-8'))
out=[]
for row in cases:
    if row['case'] not in ['A-short','C-transfer','D-correction','E-courtyard','B-long-summary','F-unknown']:continue
    msgs=[{'role':'system','content':row['system']}]+[{'role':'user' if x['user'] else 'assistant','content':x['text']} for x in row['history']]
    formatted=tokenizer.apply_chat_template(msgs,tokenize=False,add_generation_prompt=True)
    inputs=tokenizer(formatted,add_special_tokens=False,return_tensors='pt').to('cuda')
    count=inputs['input_ids'].shape[-1]
    assert count+512<=4096
    for seed in ([42,31415] if row['case']=='E-courtyard' else [42]):
        for mode in ['base','adapter']:
            set_seed(seed);start=time.perf_counter()
            if mode=='base':
                with model.disable_adapter(),torch.inference_mode():
                    generated=model.generate(**inputs,max_new_tokens=512,do_sample=True,temperature=.75,top_p=.9,top_k=40,
                        repetition_penalty=1.08,pad_token_id=tokenizer.pad_token_id)
            else:
                with torch.inference_mode():
                    generated=model.generate(**inputs,max_new_tokens=512,do_sample=True,temperature=.75,top_p=.9,top_k=40,
                        repetition_penalty=1.08,pad_token_id=tokenizer.pad_token_id)
            tokens=generated[0,count:]
            out.append({'case':row['case'],'mode':mode,'seed':seed,'inputTokens':count,'outputTokens':len(tokens),
                        'hitLimit':len(tokens)>=512,'answer':tokenizer.decode(tokens,skip_special_tokens=True),'seconds':round(time.perf_counter()-start,3),
                        'formattedInput':formatted,'activeAdapters':list(model.active_adapters) if mode=='adapter' else [],
                        'runtime':'Unsloth/PEFT, Windows CUDA, bitsandbytes 4-bit, not Android GGUF/LiteRT'})
            (AUDIT/'adapter-comparison.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
            print(row['case'],mode,seed,len(tokens),flush=True)
print('Adapter comparison complete',flush=True)
