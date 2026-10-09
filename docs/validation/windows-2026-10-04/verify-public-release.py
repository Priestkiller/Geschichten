"""Verify the published update using anonymous requests and complete file hashes."""
import hashlib
import json
from pathlib import Path
import urllib.parse
import urllib.request

OUTPUT = Path(__file__).resolve().parent
ROOT = OUTPUT.parents[2].parent
LOCAL = ROOT / "release-0.2.1"
API = "https://api.github.com/repos/Priestkiller/Geschichten/releases?per_page=100&page=1"
EXPECTED_APK_HASH = "e60bee138a77efb0f5ab7707489b645afcb36480210b295603cdca88d6f81461"
ALLOWED_HOSTS = {"api.github.com", "github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com", "github-releases.githubusercontent.com"}

def require_host(url):
    parsed = urllib.parse.urlsplit(url)
    if parsed.scheme != "https" or parsed.hostname not in ALLOWED_HOSTS or parsed.port is not None or parsed.username or parsed.password or parsed.fragment:
        raise RuntimeError("Unerwartete Downloadadresse oder Weiterleitung.")

class CheckedRedirects(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        require_host(newurl)
        return super().redirect_request(req, fp, code, msg, headers, newurl)

opener = urllib.request.build_opener(CheckedRedirects())

def open_url(url, extra=None):
    require_host(url)
    headers = {"User-Agent": "Geschichten-Android-Updater", "Accept-Encoding": "identity"}
    if urllib.parse.urlsplit(url).hostname == "api.github.com":
        headers.update({"Accept": "application/vnd.github+json", "X-GitHub-Api-Version": "2026-03-10"})
    if extra:
        headers.update(extra)
    return opener.open(urllib.request.Request(url, headers=headers), timeout=45)

with open_url(API) as response:
    releases_bytes = response.read(4 * 1024 * 1024 + 1)
if len(releases_bytes) > 4 * 1024 * 1024:
    raise RuntimeError("Veröffentlichungsliste ist zu groß.")
(OUTPUT / "github-releases.json").write_bytes(releases_bytes)
release = next(item for item in json.loads(releases_bytes) if item["tag_name"] == "v0.2.1")
assert release["draft"] is False and release["prerelease"] is True and release["published_at"]
assets = {item["name"]: item for item in release["assets"]}
assert set(assets) == {"Geschichten-0.2.1.apk", "geschichten-android-update.json"}
assert all(item["state"] == "uploaded" for item in assets.values())

description = assets["geschichten-android-update.json"]
with open_url(description["browser_download_url"]) as response:
    manifest_bytes = response.read(65537)
assert len(manifest_bytes) == description["size"] <= 65536
assert manifest_bytes == (LOCAL / "geschichten-android-update.json").read_bytes()
assert description["digest"] == "sha256:" + hashlib.sha256(manifest_bytes).hexdigest()
(OUTPUT / "public-geschichten-android-update.json").write_bytes(manifest_bytes)
manifest = json.loads(manifest_bytes)
assert manifest["applicationId"] == "dev.vincent.geschichten"
assert manifest["versionName"] == "0.2.1" and manifest["versionCode"] == 3 > 2
assert manifest["channel"] == "test" and manifest["minSdk"] == 31
assert manifest["apk"]["sha256"] == EXPECTED_APK_HASH

apk = assets[manifest["apk"]["assetName"]]
assert apk["size"] == manifest["apk"]["size"] == 138454734
assert apk["digest"] == "sha256:" + EXPECTED_APK_HASH
digest = hashlib.sha256()
size = 0
with open_url(apk["browser_download_url"], {"Range": f"bytes=0-{apk['size'] - 1}"}) as response:
    assert response.status in (200, 206)
    assert response.headers.get("Content-Encoding", "identity").lower() == "identity"
    if response.headers.get("Content-Length") is not None:
        assert int(response.headers["Content-Length"]) == apk["size"]
    if response.status == 206:
        assert response.headers["Content-Range"] == f"bytes 0-{apk['size'] - 1}/{apk['size']}"
    else:
        assert not response.headers.get("Content-Range")
    with (OUTPUT / "public-Geschichten-0.2.1.apk").open("wb") as target:
        while chunk := response.read(1024 * 1024):
            target.write(chunk)
            size += len(chunk)
            digest.update(chunk)
            if size > apk["size"]:
                raise RuntimeError("Die heruntergeladene APK ist größer als angekündigt.")
assert size == apk["size"] and digest.hexdigest() == EXPECTED_APK_HASH

summary = {"releaseUrl": release["html_url"], "draft": False, "prerelease": True, "anonymousApiRequest": True,
    "requestApiVersion": "2026-03-10", "publicManifestMatchesLocal": True, "fullPublicApkDownloaded": True,
    "apkBytes": size, "apkSha256": digest.hexdigest(), "physicalDeviceInstallTested": False}
(OUTPUT / "public-release-summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
print("BESTANDEN: Release und Updatebeschreibung sind ohne Anmeldung erreichbar.")
print("BESTANDEN: Vollständiger öffentlicher APK-Download, Dateigröße und SHA-256 stimmen mit der geprüften lokalen APK überein.")
print(release["html_url"])
