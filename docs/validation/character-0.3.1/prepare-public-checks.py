"""Reuse the previous publication checks with the final 0.3.1 identity."""
import json
from pathlib import Path

target = Path(__file__).resolve().parent
previous = target.parent / "catalog-0.3.0"
manifest = json.loads((target.parents[2].parent / "release-0.3.1/geschichten-android-update.json").read_text(encoding="utf-8"))
verification = (previous / "verify-public-release.py").read_text(encoding="utf-8")
verification = verification.replace("0.3.0", "0.3.1")
verification = verification.replace("168e9e3b4df615d5df4fa1b1aacfdabe485d0c01b1b8f1f9c39bbcd6975e970d", manifest["apk"]["sha256"])
verification = verification.replace("211095708", str(manifest["apk"]["size"]))
verification = verification.replace('manifest["versionCode"] == 4 > 3 > 2', 'manifest["versionCode"] == 5 > 4 > 3 > 2')
(target / "verify-public-release.py").write_text(verification, encoding="utf-8")
probe = (previous / "UpdateReleaseProbe.java").read_text(encoding="utf-8")
probe = probe.replace("0.3.0", "0.3.1").replace("getVersionCode() != 4", "getVersionCode() != 5")
probe = probe.replace("getVersionCode() <= 3", "getVersionCode() <= 4")
probe = probe.replace("0.2.0/0.2.1", "0.2.0/0.2.1/0.3.0")
probe = probe.replace("(Code 4)", "(Code 5)").replace("und 0.2.1 (Code 3)", ", 0.2.1 (Code 3) und 0.3.0 (Code 4)")
(target / "UpdateReleaseProbe.java").write_text(probe, encoding="utf-8")
print("Prepared anonymous publication/download and actual app-parser checks for 0.3.1 / code 5.")
