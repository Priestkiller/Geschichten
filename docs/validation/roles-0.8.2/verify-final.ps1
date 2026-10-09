$ErrorActionPreference='Stop'
$taskRoot=$PSScriptRoot
$projectRoot=(Resolve-Path (Join-Path $taskRoot '../../..')).Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$out=Join-Path $taskRoot 'apk-classes-verification'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$cp=$out+';'+$oldTools+';'+(Get-Content -LiteralPath (Join-Path $projectRoot 'docs/validation/models-0.7.0/native-classpath.txt') -Raw).Trim()
& javac -cp $cp -d $out (Join-Path $oldTools 'HostGgufBridge.java') (Join-Path $oldTools 'MemoryModelProbe.java') (Join-Path $taskRoot 'VerifyFinalRoleProbe.java')
if($LASTEXITCODE -ne 0){throw 'Verifier compilation failed'}
foreach($entry in @(@('gemma-4-e2b','gemma-4-E2B-it.litertlm'),@('huihui-qwen3-4b','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf'))) {
    & java '-Xmx6g' "-Djava.library.path=$oldTools/native-exact" -cp $cp dev.vincent.geschichten.ai.VerifyFinalRoleProbe (Join-Path $projectRoot "docs/validation/models-0.7.0/model-files/$($entry[1])") $entry[0] (Join-Path $taskRoot 'controlled-inputs.json') (Join-Path $taskRoot "production-final/$($entry[0])-answers.json") (Join-Path $out "$($entry[0])-verification.json") (Join-Path $oldTools "cache-$($entry[0])") (Join-Path $projectRoot 'app/src/main/assets/tokenizers') *> (Join-Path $out "$($entry[0])-verification.log")
    if($LASTEXITCODE -ne 0){throw "Final verifier failed: $($entry[0])"}
    Write-Output "Verified final APK planner, rendered prompt and guard: $($entry[0])"
}
