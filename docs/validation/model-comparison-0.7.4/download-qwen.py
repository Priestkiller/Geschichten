"""Download the pinned public Qwen quantization for the requested local comparison."""
import hashlib
import json
from pathlib import Path
import sys
import time
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parent
info = json.loads((ROOT / (sys.argv[1] if len(sys.argv) > 1 else "qwen-original-upstream.json")).read_text(encoding="utf-8-sig"))
filename = sys.argv[2] if len(sys.argv) > 2 else "Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"
artifact = next(row for row in info["siblings"] if row["rfilename"] == filename)
target = ROOT / filename

def checked(url):
    parsed = urllib.parse.urlsplit(url)
    host = parsed.hostname or ""
    assert parsed.scheme == "https" and not parsed.username and not parsed.password
    assert host == "huggingface.co" or host.endswith(".huggingface.co") or host.endswith(".hf.co")

class Redirects(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        checked(newurl)
        return super().redirect_request(req, fp, code, msg, headers, newurl)

def matches():
    if not target.exists() or target.stat().st_size != artifact["size"]:
        return False
    with target.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest() == artifact["lfs"]["sha256"]

if not matches():
    url = f'https://huggingface.co/{info["id"]}/resolve/{info["sha"]}/{filename}?download=true'
    checked(url)
    opener = urllib.request.build_opener(Redirects())
    request = urllib.request.Request(url, headers={"Accept-Encoding": "identity", "User-Agent": "Geschichten-Model-Comparison"})
    digest = hashlib.sha256()
    size = 0
    last = time.monotonic()
    partial = target.with_suffix(".partial")
    with opener.open(request, timeout=60) as response, partial.open("wb") as output:
        while chunk := response.read(1024 * 1024):
            size += len(chunk)
            assert size <= artifact["size"]
            output.write(chunk)
            digest.update(chunk)
            if time.monotonic() - last > 20:
                print(f"Downloaded {size:,} / {artifact['size']:,} bytes", flush=True)
                last = time.monotonic()
    assert size == artifact["size"] and digest.hexdigest() == artifact["lfs"]["sha256"]
    partial.replace(target)

result = {"repository": info["id"], "revision": info["sha"], "file": filename,
          "bytes": artifact["size"], "sha256": artifact["lfs"]["sha256"], "anonymousDownloadVerified": True}
receipt = sys.argv[3] if len(sys.argv) > 3 else "download-verified.json"
(ROOT / receipt).write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
print(json.dumps(result), flush=True)
