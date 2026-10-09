$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$prior=Join-Path $projectRoot 'docs/validation/quality-search-0.8.3'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$cp=$PSScriptRoot+';'+(Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis')+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),(Join-Path $PSScriptRoot 'delivery-classes'))+';'+(Join-Path $env:ANDROID_HOME 'platforms/android-35/android.jar')
$narrator="$prior/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/TeamNativeProbe.java" "$PSScriptRoot/TeamReplay.java"
if($LASTEXITCODE -ne 0){throw 'Replay compilation failed'}
foreach($condition in @('B','C','self')) {
    $helper=if($condition -eq 'self'){$narrator}else{"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"}
    $id=if($condition -eq 'self'){'qwen3-official-4b'}else{'huihui-qwen3-4b'}
    & java '-Xmx6g' "-Djava.library.path=$prior/native-cpu" -cp $cp dev.vincent.geschichten.ai.TeamReplay $narrator $helper $id "$PSScriptRoot/raw-team-$condition.json" $condition "$PSScriptRoot/final-controller-replay-$condition.json" *> "$PSScriptRoot/final-controller-replay-$condition.log"
    if($LASTEXITCODE -ne 0){throw "Replay failed: $condition"}
}
