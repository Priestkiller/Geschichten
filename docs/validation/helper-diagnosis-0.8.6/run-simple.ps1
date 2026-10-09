param([string]$Model='huihui-qwen3-4b',[string]$Phase='development',[double]$Temperature=0.2)
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$old=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$cp=$PSScriptRoot+';'+$old+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),(Join-Path $projectRoot 'docs\validation\team-0.8.6\delivery-classes'))
$file=if($Model -eq 'huihui-qwen3-4b'){"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"}else{"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"}
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/SimpleProbe.java"
if($LASTEXITCODE -ne 0){throw 'Simple probe compilation failed'}
& java '-Xmx6g' "-Djava.library.path=$PSScriptRoot/native-cpu-final" -cp $cp dev.vincent.geschichten.ai.SimpleProbe $file $Model "$PSScriptRoot/cases-frozen.json" "$PSScriptRoot/raw-$Phase-$Model.json" "$old/cache-$Model" "$projectRoot/app/src/main/assets/tokenizers" $Phase ($Temperature.ToString([System.Globalization.CultureInfo]::InvariantCulture)) *> "$PSScriptRoot/raw-$Phase-$Model.log"
if($LASTEXITCODE -ne 0){throw "Simple probe failed: $Model/$Phase"}
