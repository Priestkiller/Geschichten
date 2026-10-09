param([string]$Condition='A',[string]$Model='qwen3-official-4b',[string]$Cases='all')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$prior=Join-Path $projectRoot 'docs/validation/quality-search-0.8.3'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$classes=if($Condition -like 'baseline-*'){Join-Path $PSScriptRoot 'baseline-0.8.4/app/build/tmp/kotlin-classes/debug'}else{Join-Path $PSScriptRoot 'final-classes'}
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$classes)
$modelPath=switch($Model){'qwen3-official-4b'{"$prior/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"};'huihui-qwen3-4b'{"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"};'gemma-4-e2b'{"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"}}
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/QualityProbe.java"
if($LASTEXITCODE -ne 0){throw 'Probe compilation failed'}
$inputFile=if($Condition -like 'conversation-*'){Join-Path $PSScriptRoot "$Condition-scenes-frozen.json"}elseif($Condition -eq 'corrected-hybrid'){Join-Path $PSScriptRoot 'final-scenes-corrected-frozen.json'}elseif($Condition -eq 'new-free'){Join-Path $PSScriptRoot 'new-scenes-frozen.json'}elseif($Condition -like 'baseline-*'){Join-Path $PSScriptRoot 'baseline-scenes.json'}else{Join-Path $PSScriptRoot 'final-scenes-frozen.json'}
$retrieval=if($Condition -eq 'corrected-hybrid'){Join-Path $PSScriptRoot 'final-hybrid-corrected-frozen.json'}elseif($Condition -like '*lexical'){Join-Path $PSScriptRoot 'baseline-lexical.json'}elseif($Condition -eq 'baseline-hybrid'){Join-Path $PSScriptRoot 'baseline-hybrid.json'}elseif($Condition -eq 'final-hybrid'){Join-Path $PSScriptRoot 'final-hybrid.json'}else{$null}
$raw="$PSScriptRoot/raw-$Condition-$Model.json"
if(Test-Path -LiteralPath $raw){throw "Immutable result exists: $raw"}
& java '-Xmx6g' "-Djava.library.path=$prior/native-cpu" -cp $cp dev.vincent.geschichten.ai.QualityProbe $modelPath $Model $inputFile $raw "$oldTools/cache-$Model" "$projectRoot/app/src/main/assets/tokenizers" $Cases production $retrieval *> "$PSScriptRoot/raw-$Condition-$Model.log"
if($LASTEXITCODE -ne 0){throw "Probe failed: $Condition $Model"}
Write-Output "Finished $Condition $Model"
