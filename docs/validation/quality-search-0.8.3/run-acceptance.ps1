param([ValidateSet('qwen3-official-4b','huihui-qwen3-4b','gemma-4-e2b')][string]$Model='qwen3-official-4b')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),(Join-Path $PSScriptRoot 'candidate-classes'))
# Compilation is done once by freeze-candidate.ps1 before concurrent model runs.
$modelPath=switch($Model){'qwen3-official-4b'{"$PSScriptRoot/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"};'huihui-qwen3-4b'{"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"};'gemma-4-e2b'{"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"}}
$sets=,@('heldout','hold-transfer,hold-reverse,hold-family,hold-owner,hold-negation,hold-water,hold-crowd,hold-multiple,hold-correction,hold-unknown','production','')
if($Model -ne 'gemma-4-e2b'){$sets+=,@('hybrid','hold-family,hold-water,hold-crowd,hold-multiple','hybrid',"$PSScriptRoot/retrieval-heldout-results.json")}
foreach($set in $sets){
    $raw="$PSScriptRoot/acceptance-$Model-$($set[0]).json"
    if(Test-Path -LiteralPath $raw){throw "Immutable acceptance result exists: $raw"}
    $arguments=@('-Xmx6g',"-Djava.library.path=$PSScriptRoot/native-cpu",'-cp',$cp,'dev.vincent.geschichten.ai.QualityProbe',$modelPath,$Model,"$PSScriptRoot/materialized-scenes.json",$raw,"$oldTools/cache-$Model","$projectRoot/app/src/main/assets/tokenizers",$set[1],$set[2])
    if($set[3]){$arguments+=$set[3]}
    & java @arguments *> "$PSScriptRoot/acceptance-$Model-$($set[0]).log"
    if($LASTEXITCODE -ne 0){throw "Acceptance failed: $Model"}
    Write-Output "Finished acceptance $Model $($set[0])"
}
