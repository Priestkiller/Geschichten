#!/usr/bin/env python3
"""Prepare the Geschichten GitHub update manifest from a signed APK.

Runs locally with Python 3.9+, a JDK and Android SDK Build Tools. Does not
read a signing key, change the APK, contact GitHub or upload any files.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile


APPLICATION_ID = "dev.vincent.geschichten"
MANIFEST_NAME = "geschichten-android-update.json"
MAX_APK_BYTES = 1_073_741_824
TEST_CERTIFICATE_SHA256 = (
    "3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40"
)
PROJECT_ROOT = Path(__file__).resolve().parent.parent


class PreparationError(Exception):
    """A failed precondition; no manifest should be replaced."""


def arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Signierte Geschichten-APK prüfen und GitHub-Update-Manifest erzeugen."
    )
    parser.add_argument("--apk", required=True, type=Path, help="Fertige Geschichten-VERSION.apk")
    parser.add_argument("--version-name", default="0.7.4", help="Erwartete APK-Version (Standard: 0.7.4)")
    parser.add_argument("--version-code", type=int, default=13, help="Erwarteter Versionscode (Standard: 13)")
    parser.add_argument("--min-sdk", type=int, default=31, help="Erwartete Android-Mindestversion (Standard: 31)")
    parser.add_argument("--channel", choices=("test", "stable"), default="test")
    parser.add_argument("--sdk", type=Path, help="Android SDK; sonst local.properties, Umgebungsvariablen oder Standardpfad")
    parser.add_argument("--output", type=Path, help=f"Ziel mit Dateinamen {MANIFEST_NAME}; sonst neben der APK")
    parser.add_argument("--replace", action="store_true", help="Ein vorhandenes, abweichendes Manifest atomar ersetzen")
    return parser.parse_args()


def sdk_roots(explicit: Path | None) -> list[Path]:
    if explicit is not None:
        return [explicit.expanduser().resolve()]
    roots: list[Path] = []
    properties = PROJECT_ROOT / "local.properties"
    if properties.is_file():
        for line in properties.read_text(encoding="utf-8").splitlines():
            match = re.match(r"\s*sdk\.dir\s*=\s*(.+)\s*$", line)
            if match:
                # Android Studio escapes drive colons and backslashes on Windows.
                value = re.sub(r"\\([\\: =])", r"\1", match.group(1).strip())
                sdk = Path(value).expanduser()
                roots.append(sdk if sdk.is_absolute() else PROJECT_ROOT / sdk)
    for name in ("ANDROID_SDK_ROOT", "ANDROID_HOME"):
        if os.environ.get(name):
            roots.append(Path(os.environ[name]).expanduser())
    if os.environ.get("LOCALAPPDATA"):
        roots.append(Path(os.environ["LOCALAPPDATA"]) / "Android" / "Sdk")
    roots.extend((Path.home() / "Android" / "Sdk", Path.home() / "Library" / "Android" / "sdk"))
    return roots


def android_tools(explicit_sdk: Path | None) -> tuple[Path, Path]:
    suffix = ".exe" if os.name == "nt" else ""
    for sdk in sdk_roots(explicit_sdk):
        build_tools = sdk / "build-tools"
        if not build_tools.is_dir():
            continue
        versions = sorted(
            (folder for folder in build_tools.iterdir() if folder.is_dir() and re.fullmatch(r"\d+\.\d+\.\d+", folder.name)),
            key=lambda folder: tuple(int(part) for part in folder.name.split(".")),
            reverse=True,
        )
        for folder in versions:
            signer = folder / "lib" / "apksigner.jar"
            for name in ("aapt2", "aapt"):
                aapt = folder / (name + suffix)
                if aapt.is_file() and signer.is_file():
                    return aapt.resolve(), signer.resolve()
    raise PreparationError("Android Build Tools fehlen. Bitte das installierte Android SDK mit --sdk angeben.")


def java_tool() -> Path:
    suffix = ".exe" if os.name == "nt" else ""
    candidates: list[Path] = []
    if os.environ.get("JAVA_HOME"):
        candidates.append(Path(os.environ["JAVA_HOME"]) / "bin" / ("java" + suffix))
    on_path = shutil.which("java")
    if on_path:
        candidates.append(Path(on_path))
    if os.environ.get("ProgramFiles"):
        candidates.append(Path(os.environ["ProgramFiles"]) / "Android" / "Android Studio" / "jbr" / "bin" / "java.exe")
    for candidate in candidates:
        if candidate.is_file():
            return candidate.resolve()
    raise PreparationError("Java fehlt. Bitte ein JDK über JAVA_HOME oder PATH bereitstellen.")


def run_tool(command: list[str], label: str) -> str:
    try:
        result = subprocess.run(
            command, shell=False, check=False, capture_output=True,
            text=True, encoding="utf-8", errors="replace", timeout=120,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        raise PreparationError(f"{label} konnte nicht abgeschlossen werden: {error}") from error
    if result.returncode != 0:
        detail = (result.stderr.strip() or result.stdout.strip())[:1800]
        raise PreparationError(f"{label} ist fehlgeschlagen. {detail}")
    return result.stdout


def verify_apk(apk: Path, args: argparse.Namespace, aapt: Path, signer: Path) -> None:
    badging = run_tool([str(aapt), "dump", "badging", str(apk)], "APK-Metadatenprüfung")
    package_line = next((line for line in badging.splitlines() if line.startswith("package:")), "")
    package = dict(re.findall(r"(\w+)='([^']*)'", package_line))
    expected = {"name": APPLICATION_ID, "versionCode": str(args.version_code), "versionName": args.version_name}
    for key, value in expected.items():
        if package.get(key) != value:
            raise PreparationError(f"APK-Angabe {key}: erwartet {value!r}, gefunden {package.get(key)!r}.")
    sdk = re.search(r"^(?:minSdkVersion|sdkVersion):'(\d+)'$", badging, re.MULTILINE)
    if sdk is None or int(sdk.group(1)) != args.min_sdk:
        raise PreparationError(f"Die APK muss minSdk {args.min_sdk} enthalten.")
    native = re.search(r"^native-code:(.*)$", badging, re.MULTILINE)
    if native is None or set(re.findall(r"'([^']+)'", native.group(1))) != {"arm64-v8a"}:
        raise PreparationError("Diese Veröffentlichung erwartet genau die native Architektur arm64-v8a.")
    # Invoke the SDK JAR directly: no shell or platform-specific .bat wrapper,
    # and no password, signing key or credential is needed for verification.
    certificates = run_tool(
        [str(java_tool()), "-jar", str(signer), "verify", "--verbose", "--print-certs", str(apk)],
        "APK-Signaturprüfung",
    )
    digests = re.findall(r"^Signer #\d+ certificate SHA-256 digest:\s*([0-9a-fA-F]{64})\s*$", certificates, re.MULTILINE)
    if len(digests) != 1 or digests[0].lower() != TEST_CERTIFICATE_SHA256:
        raise PreparationError("Das APK-Zertifikat stimmt nicht mit der vorhandenen Geschichten-Testinstallation überein.")


def identity(path: Path) -> tuple[int, int, int, int]:
    info = path.stat()
    return info.st_dev, info.st_ino, info.st_size, info.st_mtime_ns


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def save_manifest(output: Path, payload: bytes, replace: bool) -> str:
    if output.is_symlink():
        raise PreparationError("Das Manifestziel darf kein symbolischer Link sein.")
    if output.exists():
        if not output.is_file():
            raise PreparationError("Das Manifestziel ist keine reguläre Datei.")
        if output.read_bytes() == payload:
            return "unverändert"
        if not replace:
            raise PreparationError("Ein abweichendes Manifest existiert bereits. Zum gezielten Ersetzen --replace verwenden.")
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary: Path | None = None
    try:
        with tempfile.NamedTemporaryFile(mode="wb", prefix=".update-", suffix=".tmp", dir=output.parent, delete=False) as handle:
            temporary = Path(handle.name)
            handle.write(payload)
            handle.flush()
            os.fsync(handle.fileno())
        os.replace(temporary, output)
        temporary = None
    finally:
        if temporary is not None:
            temporary.unlink(missing_ok=True)
    return "geschrieben"


def prepare(args: argparse.Namespace) -> None:
    if not re.fullmatch(r"\d+\.\d+\.\d+(?:-[A-Za-z0-9][A-Za-z0-9.-]*)?", args.version_name) or len(args.version_name) > 80:
        raise PreparationError("Versionsnamen als X.Y.Z oder X.Y.Z-zusatz angeben.")
    if not 1 <= args.version_code <= 2_147_483_647 or not 21 <= args.min_sdk <= 200:
        raise PreparationError("Versionscode oder minSdk liegt außerhalb des Updateformats.")
    if args.apk.is_symlink() or not args.apk.is_file():
        raise PreparationError("Die APK fehlt oder ist keine reguläre Datei. Zuerst die endgültige APK bauen und benennen.")
    expected_name = f"Geschichten-{args.version_name}.apk"
    if args.apk.name != expected_name:
        raise PreparationError(f"Die Release-Datei muss {expected_name} heißen; nach dem Erzeugen des Manifests nicht umbenennen.")
    apk = args.apk.resolve()
    snapshot = identity(apk)
    if not 1 <= snapshot[2] <= MAX_APK_BYTES:
        raise PreparationError("Die APK ist leer oder größer als die vom Updater unterstützten 1 GiB.")
    output = args.output or apk.with_name(MANIFEST_NAME)
    if output.name != MANIFEST_NAME:
        raise PreparationError(f"Der Manifest-Dateiname muss exakt {MANIFEST_NAME} sein.")
    if output.is_symlink():
        raise PreparationError("Das Manifestziel darf kein symbolischer Link sein.")
    output = output.resolve()
    if output == apk or (output.exists() and output.samefile(apk)):
        raise PreparationError("Das Manifest darf die APK nicht überschreiben.")
    aapt, signer = android_tools(args.sdk)
    verify_apk(apk, args, aapt, signer)
    digest = sha256_file(apk)
    if identity(apk) != snapshot:
        raise PreparationError("Die APK wurde während der Prüfung verändert. Bitte nach Ende des Builds erneut ausführen.")
    manifest = {
        "schemaVersion": 1,
        "applicationId": APPLICATION_ID,
        "versionCode": args.version_code,
        "versionName": args.version_name,
        "channel": args.channel,
        "minSdk": args.min_sdk,
        "apk": {"assetName": apk.name, "size": snapshot[2], "sha256": digest},
    }
    payload = (json.dumps(manifest, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    status = save_manifest(output, payload, args.replace)
    print(f"Manifest {status}: {output}")
    print(f"APK: {apk.name} | {snapshot[2]} Byte | SHA-256 {digest}")
    print(f"Paket/Version/Signatur geprüft: {APPLICATION_ID}, {args.version_name} ({args.version_code})")
    print(f"GitHub: prerelease={'true' if args.channel == 'test' else 'false'}, draft=false nach Veröffentlichung.")
    print("Nur APK und Manifest als Release-Dateien hochladen. Es wurde nichts veröffentlicht.")


if __name__ == "__main__":
    try:
        prepare(arguments())
    except (PreparationError, OSError, ValueError) as error:
        print(f"Fehler: {error}", file=sys.stderr)
        sys.exit(1)
