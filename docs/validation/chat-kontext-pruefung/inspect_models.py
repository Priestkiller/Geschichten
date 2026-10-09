"""Public downloaded model artifacts only. No user chats, credentials or private keystore."""
import hashlib,json,pathlib
from collections import Counter
from gguf import GGUFReader,GGMLQuantizationType
ROOT=pathlib.Path(__file__).resolve().parents[3]
FILES=ROOT/'docs/validation/models-0.7.0/model-files'
out=[]
names=['Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf','Gemma-3-it-4B-Uncensored-D_AU-Q4_0.gguf',
       'gemma-4-E2B-it.litertlm','Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm','Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm']
for name in names:
    path=FILES/name
    sha=hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda:f.read(8*1024*1024),b''):sha.update(chunk)
    row={'file':name,'bytes':path.stat().st_size,'sha256':sha.hexdigest()}
    if path.suffix=='.gguf':
        reader=GGUFReader(path)
        keep=['general.architecture','general.file_type','general.name','general.quantization_version','tokenizer.ggml.model','tokenizer.ggml.pre',
              'tokenizer.ggml.bos_token_id','tokenizer.ggml.eos_token_id','tokenizer.ggml.add_bos_token','tokenizer.ggml.add_eos_token','tokenizer.chat_template']
        row['metadata']={key:reader.fields[key].contents() for key in reader.fields if key in keep or key.endswith('.context_length')}
        row['tensorTypes']=dict(Counter(GGMLQuantizationType(t.tensor_type).name for t in reader.tensors))
    out.append(row)
    (ROOT/'docs/validation/chat-kontext-pruefung/model-artifacts.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
    print('Inspected',name)
