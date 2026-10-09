"""Adapt the proven anonymous release check to the final APK identity."""
import json
from pathlib import Path

out = Path(__file__).resolve().parent
old = out.parent / "worlds-0.4.0"
manifest = json.loads((out.parents[2].parent / "release-0.5.0/geschichten-android-update.json").read_text(encoding="utf-8"))
script = (old / "verify-public-release.py").read_text(encoding="utf-8")
script = script.replace("0.4.0", "0.5.0")
script = script.replace("3942596b01ba21a6258fb96cae9e9390522f4b2a96303b3f7062d0fd05d6be0d", manifest["apk"]["sha256"])
script = script.replace("306719750", str(manifest["apk"]["size"]))
script = script.replace('manifest["versionCode"] == 6 > 5 > 4 > 3 > 2', 'manifest["versionCode"] == 7 > 6 > 5 > 4 > 3 > 2')
(out / "verify-public-release.py").write_text(script, encoding="utf-8")
probe = (old / "UpdateReleaseProbe.java").read_text(encoding="utf-8")
probe = probe.replace("0.4.0", "0.5.0")
probe = probe.replace("getVersionCode() != 6", "getVersionCode() != 7").replace("getVersionCode() <= 5", "getVersionCode() <= 6")
probe = probe.replace("0.2.0/0.2.1/0.3.0/0.3.1", "0.2.0/0.2.1/0.3.0/0.3.1/0.4.0")
probe = probe.replace("Release 0.5.0 (Code 6)", "Release 0.5.0 (Code 7)")
probe = probe.replace("und 0.3.1 (Code 5).", ", 0.3.1 (Code 5) und 0.4.0 (Code 6).")
(out / "UpdateReleaseProbe.java").write_text(probe, encoding="utf-8")
print("Prepared anonymous release verification and actual app parser check for 0.5.0/code 7.")
