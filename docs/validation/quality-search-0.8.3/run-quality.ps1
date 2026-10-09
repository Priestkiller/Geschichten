param([string]$Phase='development')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),(Join-Path $PSScriptRoot 'baseline-classes'))
& javac -cp $cp -d $PSScriptRoot "$oldTools/HostGgufBridge.java" "$oldTools/MemoryModelProbe.java" "$PSScriptRoot/QualityProbe.java"
if($LASTEXITCODE -ne 0){throw 'Quality probe compile failed'}
$models=@(@('qwen3-official-4b',"$PSScriptRoot/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"),@('huihui-qwen3-4b',"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"),@('gemma-4-e2b',"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"))
$cases='dev-transfer,dev-family,dev-negation,dev-panic'
$modes=@('short','production')
if($Phase -eq 'heldout'){$cases='hold-transfer,hold-reverse,hold-family,hold-owner,hold-negation,hold-water,hold-crowd,hold-multiple,hold-correction,hold-unknown';$modes=@('production')}
foreach($entry in $models){foreach($mode in $modes){
    $raw="$PSScriptRoot/$Phase-$($entry[0])-$mode.json"
    if(Test-Path -LiteralPath $raw){continue}
    & java '-Xmx6g' "-Djava.library.path=$oldTools/native-exact" -cp $cp dev.vincent.geschichten.ai.QualityProbe $entry[1] $entry[0] "$PSScriptRoot/new-scenes.json" $raw "$oldTools/cache-$($entry[0])" "$projectRoot/app/src/main/assets/tokenizers" $cases $mode *> "$PSScriptRoot/$Phase-$($entry[0])-$mode.log"
    if($LASTEXITCODE -ne 0){throw "Quality probe failed: $($entry[0])"}
    Write-Output "Finished $Phase $($entry[0]) $mode"
}}
