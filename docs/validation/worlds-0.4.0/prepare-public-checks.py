"""Adapt the previous anonymous download check to the final package identity."""
import json
from pathlib import Path

out = Path(__file__).resolve().parent
old = out.parent / "character-0.3.1"
manifest = json.loads((out.parents[2].parent / "release-0.4.0/geschichten-android-update.json").read_text(encoding="utf-8"))
script = (old / "verify-public-release.py").read_text(encoding="utf-8")
script = script.replace("0.3.1", "0.4.0")
script = script.replace("797cf6158e4ecfe937a0e2c6f6026210063cff08056d6327ca306674cd214f0a", manifest["apk"]["sha256"])
script = script.replace("211161244", str(manifest["apk"]["size"]))
script = script.replace('manifest["versionCode"] == 5 > 4 > 3 > 2', 'manifest["versionCode"] == 6 > 5 > 4 > 3 > 2')
(out / "verify-public-release.py").write_text(script, encoding="utf-8")
probe = (old / "UpdateReleaseProbe.java").read_text(encoding="utf-8")
probe = probe.replace("0.3.1", "0.4.0")
probe = probe.replace("getVersionCode() != 5", "getVersionCode() != 6").replace("getVersionCode() <= 4", "getVersionCode() <= 5")
probe = probe.replace("0.2.0/0.2.1/0.3.0", "0.2.0/0.2.1/0.3.0/0.3.1")
probe = probe.replace("Release 0.4.0 (Code 5)", "Release 0.4.0 (Code 6)")
probe = probe.replace("und 0.3.0 (Code 4).", ", 0.3.0 (Code 4) und 0.3.1 (Code 5).")
(out / "UpdateReleaseProbe.java").write_text(probe, encoding="utf-8")
print("Prepared anonymous release/download verification and actual app parser probe for 0.4.0/code 6.")
