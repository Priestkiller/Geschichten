"""Download exact public artifacts for desktop compatibility probes; never package them."""
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
from pathlib import Path
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parent.parent / "models-0.7.0"
FILES = [("Gemma-3-it-4B-Uncensored-DBL-X-GGUF", "Gemma-3-it-4B-Uncensored-D_AU-Q4_k_m.gguf")]

def checked(url):
    p = urllib.parse.urlsplit(url)
    host = p.hostname or ""
    assert p.scheme == "https" and not p.username and not p.password
    assert host == "huggingface.co" or host.endswith(".huggingface.co") or host.endswith(".hf.co")

class Redirects(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        checked(newurl)
        return super().redirect_request(req, fp, code, msg, headers, newurl)

def download(item):
    slug, filename = item
    info = json.loads((ROOT / (slug + "-upstream.json")).read_text(encoding="utf-8-sig"))
    file = next(x for x in info["siblings"] if x["rfilename"] == filename)
    target = ROOT / "model-files" / filename
    target.parent.mkdir(exist_ok=True)
    def matches():
        if not target.exists() or target.stat().st_size != file["size"]:
            return False
        with target.open("rb") as source:
            return hashlib.file_digest(source, "sha256").hexdigest() == file["lfs"]["sha256"]
    if not matches():
        url = f'https://huggingface.co/{info["id"]}/resolve/{info["sha"]}/{filename}?download=true'
        checked(url)
        request = urllib.request.Request(url, headers={"Accept-Encoding": "identity", "User-Agent": "Geschichten-Model-Validation"})
        opener = urllib.request.build_opener(Redirects())
        digest = hashlib.sha256()
        size = 0
        with opener.open(request, timeout=45) as response, target.open("wb") as output:
            while chunk := response.read(1024 * 1024):
                size += len(chunk)
                assert size <= file["size"]
                output.write(chunk)
                digest.update(chunk)
        assert size == file["size"] and digest.hexdigest() == file["lfs"]["sha256"]
    result = {"repository": info["id"], "revision": info["sha"], "file": filename,
              "bytes": file["size"], "sha256": file["lfs"]["sha256"], "anonymousDownloadVerified": True}
    print(json.dumps(result), flush=True)
    return result

with ThreadPoolExecutor(max_workers=3) as pool:
    results = list(pool.map(download, FILES))
(Path(__file__).resolve().parent / "alternative-quantization-download.json").write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
