param([string[]] $Only = @(), [switch] $AfterOnly, [string] $Label = '', [string] $TestInputs = '')
$ErrorActionPreference='Stop'
$taskRoot=$PSScriptRoot
$projectRoot=(Resolve-Path (Join-Path $taskRoot '../../..')).Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$taskOutput=if($Label){Join-Path $taskRoot $Label}else{$taskRoot}
$taskInputs=if($TestInputs){(Resolve-Path -LiteralPath $TestInputs).Path}else{Join-Path $taskRoot 'model-cases.json'}
New-Item -ItemType Directory -Path $taskOutput -Force | Out-Null
$taskClasses=Join-Path $taskOutput 'production-classes'
New-Item -ItemType Directory -Path $taskClasses -Force | Out-Null
Copy-Item -Path (Join-Path $projectRoot 'app/build/tmp/kotlin-classes/debug/*') -Destination $taskClasses -Recurse -Force
$taskCp=$taskRoot+';'+(Get-Content -LiteralPath (Join-Path $projectRoot 'docs/validation/models-0.7.0/native-classpath.txt') -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$taskClasses)
& javac -cp $taskCp -d $taskRoot (Join-Path $taskRoot 'HostGgufBridge.java') (Join-Path $taskRoot 'NativeTokenizerProbe.java') (Join-Path $taskRoot 'MemoryModelProbe.java')
if($LASTEXITCODE -ne 0){throw 'Model probe compilation failed'}
$taskModels=@(
    @('gemma-4-e2b','gemma-4-E2B-it.litertlm'),
    @('huihui-qwen3-4b','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf'),
    @('dolphin3-llama3.2-3b','Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf'),
    @('gemma3-davidau-4b','Gemma-3-it-4B-Uncensored-D_AU-Q4_0.gguf'),
    @('qwen2.5-1.5b','Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm'),
    @('qwen3-0.6b','Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm')
)
foreach($entry in $taskModels) {
    if($Only.Count -gt 0 -and $entry[0] -notin $Only){continue}
    $taskMode=if($AfterOnly){'after-only'}else{'both'}
    & java '-Xmx6g' "-Djava.library.path=$taskRoot/native-exact" -cp $taskCp dev.vincent.geschichten.ai.MemoryModelProbe (Join-Path $projectRoot "docs/validation/models-0.7.0/model-files/$($entry[1])") $entry[0] $taskInputs (Join-Path $taskOutput "$($entry[0])-answers.json") (Join-Path $taskRoot "cache-$($entry[0])") (Join-Path $projectRoot 'app/src/main/assets/tokenizers') $taskMode *> (Join-Path $taskOutput "$($entry[0])-run.log")
    if($LASTEXITCODE -ne 0){throw "Model probe failed: $($entry[0])"}
    Write-Output "Finished model: $($entry[0])"
}
