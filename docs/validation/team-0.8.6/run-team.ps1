param([string]$Condition='B')
$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$prior=Join-Path $projectRoot 'docs/validation/quality-search-0.8.3'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$classes=Join-Path $PSScriptRoot 'phase-b-final-classes'
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$classes)+';'+(Join-Path $env:ANDROID_HOME 'platforms/android-35/android.jar')
$narrator="$prior/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"
$helper=if($Condition -eq 'self'){$narrator}else{"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"}
$id=if($Condition -eq 'self'){'qwen3-official-4b'}else{'huihui-qwen3-4b'}
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/TeamNativeProbe.java"
if($LASTEXITCODE -ne 0){throw 'Team compilation failed'}
& java '-Xmx6g' "-Djava.library.path=$prior/native-cpu" -cp $cp dev.vincent.geschichten.ai.TeamNativeProbe $narrator $helper $id "$PSScriptRoot/phase-b-scenes-frozen.json" "$PSScriptRoot/raw-team-$Condition.json" $Condition *> "$PSScriptRoot/raw-team-$Condition.log"
if($LASTEXITCODE -ne 0){throw 'Team probe failed'}
Write-Output "Finished team $Condition"
