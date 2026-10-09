$ErrorActionPreference='Stop'
$taskRoot=$PSScriptRoot
$projectRoot=(Resolve-Path (Join-Path $taskRoot '../../..')).Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$outputRoot=Join-Path $taskRoot 'production-final'
$classes=Join-Path $outputRoot 'production-classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
Copy-Item -Path (Join-Path $projectRoot 'app/build/tmp/kotlin-classes/debug/*') -Destination $classes -Recurse -Force
$cp=$outputRoot+';'+$oldTools+';'+(Get-Content -LiteralPath (Join-Path $projectRoot 'docs/validation/models-0.7.0/native-classpath.txt') -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$classes)
& javac -cp $cp -d $outputRoot (Join-Path $oldTools 'HostGgufBridge.java') (Join-Path $oldTools 'MemoryModelProbe.java') (Join-Path $taskRoot 'ProductionRoleProbe.java')
if($LASTEXITCODE -ne 0){throw 'Probe compilation failed'}
foreach($entry in @(@('gemma-4-e2b','gemma-4-E2B-it.litertlm'),@('huihui-qwen3-4b','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf'))) {
    & java '-Xmx6g' "-Djava.library.path=$oldTools/native-exact" -cp $cp dev.vincent.geschichten.ai.ProductionRoleProbe (Join-Path $projectRoot "docs/validation/models-0.7.0/model-files/$($entry[1])") $entry[0] (Join-Path $taskRoot 'controlled-inputs.json') (Join-Path $outputRoot "$($entry[0])-answers.json") (Join-Path $oldTools "cache-$($entry[0])") (Join-Path $projectRoot 'app/src/main/assets/tokenizers') *> (Join-Path $outputRoot "$($entry[0])-answers.log")
    if($LASTEXITCODE -ne 0){throw "Production probe failed: $($entry[0])"}
    Write-Output "Finished automatic production probe: $($entry[0])"
}
