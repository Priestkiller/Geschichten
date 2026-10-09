$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$prior=Join-Path $projectRoot 'docs/validation/quality-search-0.8.3'
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim()
& javac -encoding UTF-8 -cp $cp -d $PSScriptRoot "$PSScriptRoot/FinalPromptVerifier.java"
if($LASTEXITCODE -ne 0){throw 'Verifier compile failed'}
foreach($entry in @(@('qwen3-official-4b',"$prior/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"),@('huihui-qwen3-4b',"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"),@('gemma-4-e2b',"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"))) {
 & java '-Xmx6g' "-Djava.library.path=$prior/native-cpu" -cp $cp dev.vincent.geschichten.ai.FinalPromptVerifier $entry[1] $entry[0] $PSScriptRoot "$oldTools/cache-$($entry[0])" "$projectRoot/app/src/main/assets/tokenizers" *> "$PSScriptRoot/final-input-check-$($entry[0]).log"
 if($LASTEXITCODE -ne 0){throw "Final input mismatch: $($entry[0])"}
 Get-Content -LiteralPath "$PSScriptRoot/final-input-check-$($entry[0]).log" -Tail 1
}
