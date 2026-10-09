$ErrorActionPreference='Stop'
$taskRoot=$PSScriptRoot
$projectRoot=(Resolve-Path (Join-Path $taskRoot '../../..')).Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$classes=Join-Path $taskRoot 'production-081'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
if(-not (Test-Path -LiteralPath (Join-Path $classes 'dev/vincent/geschichten/memory/MemoryPrompt.class'))) {
    Copy-Item -Path (Join-Path $projectRoot 'docs/validation/active-memory-0.8.1/models-final/production-classes/*') -Destination $classes -Recurse -Force
}
$cp=$taskRoot+';'+$oldTools+';'+(Get-Content -LiteralPath (Join-Path $projectRoot 'docs/validation/models-0.7.0/native-classpath.txt') -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$classes)
& javac -cp $cp -d $taskRoot (Join-Path $taskRoot 'diagnostic-081/HostGgufBridge.java') (Join-Path $taskRoot 'diagnostic-081/MemoryModelProbe.java') (Join-Path $taskRoot 'RoleComparisonProbe.java')
if($LASTEXITCODE -ne 0){throw 'Probe compilation failed'}
foreach($entry in @(@('gemma-4-e2b','gemma-4-E2B-it.litertlm'),@('huihui-qwen3-4b','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf'))) {
    & java '-Xmx6g' "-Djava.library.path=$oldTools/native-exact" -cp $cp dev.vincent.geschichten.ai.RoleComparisonProbe (Join-Path $projectRoot "docs/validation/models-0.7.0/model-files/$($entry[1])") $entry[0] (Join-Path $taskRoot 'controlled-inputs.json') (Join-Path $taskRoot "$($entry[0])-comparison.json") (Join-Path $oldTools "cache-$($entry[0])") (Join-Path $projectRoot 'app/src/main/assets/tokenizers') (Join-Path $projectRoot 'docs/validation/active-memory-0.8.1/models-final') *> (Join-Path $taskRoot "$($entry[0])-comparison.log")
    if($LASTEXITCODE -ne 0){throw "Comparison failed: $($entry[0])"}
    Write-Output "Finished A/B/C: $($entry[0])"
}
