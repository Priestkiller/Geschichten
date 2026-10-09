param([int]$Round=1)
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$prior=Join-Path $projectRoot 'docs/validation/quality-search-0.8.3'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$cp=$PSScriptRoot+';'+(Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis')+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),(Join-Path $PSScriptRoot 'delivery-classes'))+';'+(Join-Path $env:ANDROID_HOME 'platforms/android-35/android.jar')
$narrator="$prior/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"
$helper="$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/TeamNativeProbe.java"
if($LASTEXITCODE -ne 0){throw 'Dialogue compilation failed'}
& java '-Xmx6g' "-Djava.library.path=$prior/native-cpu" -cp $cp dev.vincent.geschichten.ai.TeamNativeProbe $narrator $helper 'huihui-qwen3-4b' "$PSScriptRoot/dialogue-$Round-scenes-frozen.json" "$PSScriptRoot/raw-dialogue-$Round.json" 'C' *> "$PSScriptRoot/raw-dialogue-$Round.log"
if($LASTEXITCODE -ne 0){throw 'Dialogue inference failed'}
