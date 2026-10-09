param([string]$Model='huihui-qwen3-4b')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$prior=Join-Path $projectRoot 'docs/validation/quality-search-0.8.3'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$classes=Join-Path $PSScriptRoot 'phase-a-classes'
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$classes)+';'+(Join-Path $env:ANDROID_HOME 'platforms/android-35/android.jar')
$modelPath=if($Model -eq 'huihui-qwen3-4b'){"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"}else{"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"}
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/HelperProbe.java"
if($LASTEXITCODE -ne 0){throw 'Helper compilation failed'}
& java '-Xmx6g' "-Djava.library.path=$prior/native-cpu" -cp $cp dev.vincent.geschichten.ai.HelperProbe $modelPath $Model "$PSScriptRoot/phase-a-complete-frozen.json" "$PSScriptRoot/raw-phase-a-$Model.json" "$oldTools/cache-$Model" "$projectRoot/app/src/main/assets/tokenizers" *> "$PSScriptRoot/raw-phase-a-$Model.log"
if($LASTEXITCODE -ne 0){throw 'Helper probe failed'}
Write-Output "Finished helper $Model"
