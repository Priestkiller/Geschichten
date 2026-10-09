$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$out=Join-Path $PSScriptRoot 'final-inputs-classes';New-Item -ItemType Directory -Path $out -Force | Out-Null
$cp=$out+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim()
& javac -cp $cp -d $out "$oldTools/HostGgufBridge.java" "$oldTools/MemoryModelProbe.java" "$PSScriptRoot/verify-all-prompts.java"
if($LASTEXITCODE -ne 0){throw 'Final input verifier compile failed'}
foreach($entry in @(@('qwen3-official-4b',"$PSScriptRoot/model-files/Qwen_Qwen3-4B-Instruct-2507-Q4_K_M.gguf"),@('huihui-qwen3-4b',"$projectRoot/docs/validation/models-0.7.0/model-files/huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf"),@('gemma-4-e2b',"$projectRoot/docs/validation/models-0.7.0/model-files/gemma-4-E2B-it.litertlm"))) {
    & java '-Xmx6g' "-Djava.library.path=$PSScriptRoot/native-cpu" -cp $cp dev.vincent.geschichten.ai.VerifyAllPrompts $entry[1] $entry[0] $PSScriptRoot "$oldTools/cache-$($entry[0])" "$projectRoot/app/src/main/assets/tokenizers" *> "$PSScriptRoot/final-prompts-$($entry[0]).log"
    if($LASTEXITCODE -ne 0){throw "Final input mismatch: $($entry[0])"}
    Write-Output "Verified all final selected inputs: $($entry[0])"
}
