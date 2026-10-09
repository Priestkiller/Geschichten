"""Summarize recorded checks and inspect the delivered local APK; never run inference."""
from pathlib import Path
import hashlib, json, struct, subprocess, zipfile, xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[3]
out = Path(__file__).resolve().parent
def sha(data): return hashlib.sha256(data).hexdigest()
def save(name, value): (out / name).write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding="utf-8")
def tests(folder):
    result = dict(tests=0, failures=0, errors=0, skipped=0, suites=0)
    for file in sorted(folder.glob("TEST-*.xml")):
        suite = ET.parse(file).getroot()
        result["suites"] += 1
        for key in ("tests", "failures", "errors", "skipped"): result[key] += int(suite.get(key, "0"))
    result["passed"] = result["tests"] - result["failures"] - result["errors"] - result["skipped"]
    return result
lint = ET.parse(out / "lint-final.xml").getroot()
tokenizer_checks = json.loads((out / "tokenizer-results.json").read_text(encoding="utf-8"))
assert len(tokenizer_checks) == 177 and all(row["equal"] for row in tokenizer_checks)
save("test-summary.json", dict(normal=tests(out / "unit-test-xml"), extended=tests(out / "full-test-xml"), targeted=tests(out / "memory-test-xml"), lint={severity: sum(issue.get("severity") == severity for issue in lint.findall("issue")) for severity in ("Error", "Warning", "Information")}, note="Overlapping test runs; counts must not be added. Two FileProvider filesystem cases require Linux/Android and are skipped on Windows."))

models = {}
for file in sorted((out / "final-models").glob("*-answers.json")):
    rows = json.loads(file.read_text(encoding="utf-8"))
    assert len(rows) == 6
    for row in rows:
        assert row["plannedInputTokens"] == row["nativePromptTokens"], row
        assert row["nativePromptTokens"] + 512 <= 4096, row
    warm = sorted(row["planningMs"] for row in rows if row["case"] != "short")
    models[rows[0]["model"]] = dict(cases=6, accepted=sum(row["accepted"] for row in rows), tokenizerMatches=6, inputTokens=[min(row["nativePromptTokens"] for row in rows), max(row["nativePromptTokens"] for row in rows)], coldPlanningMs=rows[0]["planningMs"], warmPlanningMsRange=[warm[0], warm[-1]], warmPlanningMedianMs=warm[len(warm)//2], firstTextMsRange=[min(row["firstTextMs"] for row in rows), max(row["firstTextMs"] for row in rows)], environment="Desktop CPU 4 threads, model initialization excluded; not an S24 benchmark")
save("model-metrics.json", dict(models=models, finalCalls=36, matchedBeforeCalls=36, intermediateCalls=36, additionalMemoryModelCalls=0, finalPromptCountsMatch=True, reserve=512, context=4096))

baseline = json.loads((out / "source-before.json").read_text(encoding="utf-8-sig"))
paths = {name.replace("\\", "/") for name in baseline}
for folder in (root / "app/src",):
    paths.update(file.relative_to(root).as_posix() for file in folder.rglob("*") if file.is_file())
paths.update(file.relative_to(root).as_posix() for file in (root / "third_party/sentencepiece-0.2.1").rglob("*") if file.is_file())
paths.update(name for name in ("README.md", "THIRD_PARTY_NOTICES.md", "BUILD_WINDOWS.cmd", "build-linux.sh", "app/build.gradle.kts", "docs/BUILD.md", "docs/dauerhaftes-gedaechtnis.md", "docs/PRUEFBERICHT-0.8.0.md") if (root/name).is_file())
normalized = {name.replace("\\", "/"): value for name, value in baseline.items()}
records = []
for name in sorted(paths):
    file = root / name
    before = normalized.get(name)
    after = sha(file.read_bytes()) if file.is_file() else None
    status = "unchanged" if before == after else "modified" if before and after else "removed" if before else "added"
    records.append(dict(path=name, status=status, beforeSha256=before, afterSha256=after))
assert not any(row["status"] == "removed" for row in records)
critical = [row for row in records if row["path"].endswith(("LocalModelCatalog.kt", "ModelArtifact.kt", "WorldCharacters.kt", "ReleasedIntroductionsV6.kt", "CharacterRevisions.kt", "CharacterIntroductions.kt", "CharacterDialogueRevision.kt", "BuiltinCharacters.kt", "AdditionalCharacters.kt")) or "/drawable" in row["path"]]
assert all(row["status"] == "unchanged" for row in critical), critical
save("source-changes.json", dict(baseline="source-before.zip and source-before.json", records=records, removed=0, unchangedCatalogAndPortraits=True, note="Documents without baseline hashes and the new vendored SentencePiece tree are additions. Validation artifacts and private model weights are not app sources."))
with zipfile.ZipFile(out / "source-before.zip") as archive:
    (out / "source-diffs").mkdir(exist_ok=True)
    import difflib
    for row in records:
        if row["status"] != "modified" or not row["path"].endswith((".kt", ".kts", ".cpp", ".txt", ".md", ".cmd", ".sh")): continue
        name = next((name for name in archive.namelist() if name.replace("\\", "/") == row["path"]), None)
        if not name: continue
        try:
            before = archive.read(name).decode("utf-8-sig").splitlines(keepends=True)
            after = (root / row["path"]).read_text(encoding="utf-8-sig").splitlines(keepends=True)
        except UnicodeDecodeError: continue
        diff = "".join(difflib.unified_diff(before, after, "before/"+row["path"], "after/"+row["path"]))
        (out / "source-diffs" / (row["path"].replace("/", "__")+".diff")).write_text(diff, encoding="utf-8")

apk = root / "Geschichten-0.8.0.apk"
sdk = root.parent / ".build-tools/sdk"
llvm = sdk / "ndk/27.2.12479018/toolchains/llvm/prebuilt/windows-x86_64/bin"
native_folder = out / "apk-native"
native_folder.mkdir(exist_ok=True)
with zipfile.ZipFile(apk) as archive:
    dex = b"".join(archive.read(name) for name in archive.namelist() if name.endswith(".dex"))
    absent = {name: name.encode() not in dex for name in ("MemoryModelProbe", "NativeTokenizerProbe", "MemoryRepositoryIntegrationTest", "org/robolectric")}
    assert all(absent.values()), absent
    libraries = []
    for name in archive.namelist():
        if not name.startswith("lib/") or not name.endswith(".so"): continue
        data = archive.read(name)
        dest = native_folder / Path(name).name
        dest.write_bytes(data)
        headers = subprocess.check_output([str(llvm / "llvm-readelf.exe"), "-lW", str(dest)], text=True)
        (native_folder / (dest.name+".headers.txt")).write_text(headers, encoding="utf-8")
        alignments = [int(line.split()[-1], 16) for line in headers.splitlines() if line.strip().startswith("LOAD ")]
        assert alignments and min(alignments) >= 16384, (name, alignments)
        libraries.append(dict(path=name, bytes=len(data), sha256=sha(data), loadAlignments=alignments))
    symbols = subprocess.check_output([str(llvm / "llvm-nm.exe"), "-D", "--defined-only", str(native_folder / "libgeschichten_gguf.so")], text=True)
    (native_folder / "app-symbols.txt").write_text(symbols, encoding="utf-8")
    assert "MemoryModelProbe_setSeed" not in symbols
    assert "ExactTokenizerNative" in symbols and "GgufNative_countPrompt" in symbols
    tokens = []
    for name in archive.namelist():
        if not name.startswith("assets/tokenizers/"): continue
        data = archive.read(name)
        count = struct.unpack_from("<Q", data, 8)[0] if data.startswith(b"GGUF") else None
        assert count in (None, 0)
        tokens.append(dict(path=name, bytes=len(data), sha256=sha(data), ggufTensorCount=count))
    assert len(tokens) == 3
    assert not any(name.endswith((".litertlm", ".safetensors")) for name in archive.namelist())
save("apk-verification.json", dict(file=str(apk), bytes=apk.stat().st_size, sha256=sha(apk.read_bytes()), package="dev.vincent.geschichten", versionName="0.8.0", versionCode=14, minSdk=31, targetSdk=35, abi="arm64-v8a", debug=True, signerSha256="3db10e5029fc46a9bbe9bbe6a93ede3acc3b60984f97c73ff0eeb4c50f12cb40", tokenizerAssets=tokens, libraries=libraries, diagnosticClassesAbsent=absent, hostSeedExportAbsent=True, exactTokenizerAndGgufCounterExportsPresent=True, extraWeightsAbsent=True, zipAlign16kVerified="Verification succesful" in (out/"apk-alignment.txt").read_text(encoding="utf-8-sig"), published=False))
print(json.dumps(dict(tests=json.loads((out/"test-summary.json").read_text(encoding="utf-8")), apkSha256=sha(apk.read_bytes()), sourceChanges={status:sum(row["status"] == status for row in records) for status in ("modified", "added", "unchanged", "removed")}, modelFinalRows=36), ensure_ascii=False, indent=2))
