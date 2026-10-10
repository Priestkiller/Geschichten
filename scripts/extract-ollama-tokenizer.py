"""Extract GGUF metadata only; retain no inference tensors or private machine paths."""
import argparse, hashlib, io, json, pathlib, struct

parser = argparse.ArgumentParser()
parser.add_argument('model_blob',type=pathlib.Path)
args = parser.parse_args()
root = pathlib.Path(__file__).resolve().parents[1]
weights = 'bb722270d54346adc198f213851dbe0207e87c3ecf2a0aff4d92262726215391'
assert args.model_blob.name == 'sha256-'+weights
with args.model_blob.open('rb') as file:
    header = file.read(32 * 1024 * 1024)
stream = io.BytesIO(header)
sizes = {0:1,1:1,2:2,3:2,4:4,5:4,6:4,7:1,10:8,11:8,12:8}
def integer(fmt):
    value = stream.read(struct.calcsize(fmt))
    if len(value) != struct.calcsize(fmt): raise ValueError('Metadata exceeds header buffer')
    return struct.unpack(fmt,value)[0]
def string(): return stream.read(integer('<Q'))
def skip(kind):
    if kind == 8: stream.seek(integer('<Q'),1)
    elif kind == 9:
        subtype, count = integer('<I'), integer('<Q')
        if subtype in sizes: stream.seek(sizes[subtype] * count,1)
        else:
            for _ in range(count): skip(subtype)
    else: stream.seek(sizes[kind],1)
assert stream.read(4) == b'GGUF'
version = integer('<I')
assert version == 3
integer('<Q')
count = integer('<Q')
for _ in range(count):
    string(); skip(integer('<I'))
end = stream.tell()
assert end <= len(header)
data = b'GGUF' + struct.pack('<IQQ',version,0,count) + header[24:end]
data += b'\0' * (-len(data) % 32)
target = root / 'app/src/main/assets/tokenizers/gemma4-12b-ollama.gguf'
target.write_bytes(data)
result = {'weightsBlob':'sha256-'+weights,'tokenizerSHA256':hashlib.sha256(data).hexdigest(),
          'bytes':len(data),'tensors':0,'metadataKeys':count}
(root / 'docs/ollama-tokenizer.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result),flush=True)
