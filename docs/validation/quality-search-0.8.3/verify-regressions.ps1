$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$roles=Join-Path $projectRoot 'docs/validation/roles-0.8.2'
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$env:PATH=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin')+';'+$env:PATH
$out=Join-Path $PSScriptRoot 'regression-082'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$cp=$out+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim()
& javac -cp $cp -d $out "$oldTools/HostGgufBridge.java" "$oldTools/MemoryModelProbe.java" "$roles/VerifyFinalRoleProbe.java"
if($LASTEXITCODE -ne 0){throw 'Regression compile failed'}
foreach($entry in @(@('gemma-4-e2b','gemma-4-E2B-it.litertlm'),@('huihui-qwen3-4b','huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf'))) {
    & java '-Xmx6g' "-Djava.library.path=$oldTools/native-exact" -cp $cp dev.vincent.geschichten.ai.VerifyFinalRoleProbe "$projectRoot/docs/validation/models-0.7.0/model-files/$($entry[1])" $entry[0] "$roles/controlled-inputs.json" "$roles/production-final/$($entry[0])-answers.json" "$out/$($entry[0]).json" "$oldTools/cache-$($entry[0])" "$projectRoot/app/src/main/assets/tokenizers" *> "$out/$($entry[0]).log"
    if($LASTEXITCODE -ne 0){throw "Selected prompt regression failed: $($entry[0])"}
    Write-Output "0.8.2 selected prompts, actual tokens and guards match: $($entry[0])"
}
