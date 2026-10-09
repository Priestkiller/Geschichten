param([ValidateSet('gguf', 'litert')] [string] $Runtime)
$ErrorActionPreference = 'Stop'
$auditRoot = $PSScriptRoot
$projectRoot = (Resolve-Path (Join-Path $auditRoot '..\..\..')).Path
$workspaceRoot = Split-Path $projectRoot
. (Join-Path $workspaceRoot 'Build-Umgebung.ps1')
$env:PATH = (Join-Path $workspaceRoot '.build-tools\native-probes\llvm-mingw-20260922-ucrt-x86_64\bin') + ';' + $env:PATH
$classPath = $auditRoot + ';' + (Get-Content -LiteralPath (Join-Path $projectRoot 'docs\validation\models-0.7.0\native-classpath.txt') -Raw).Trim()
$files = Join-Path $projectRoot 'docs\validation\models-0.7.0\model-files'
$cases = Join-Path $auditRoot 'inputs-with-overflow.json'
if ($Runtime -eq 'gguf') {
    & javac -cp $classPath -d $auditRoot (Join-Path $auditRoot 'HostGgufBridge.java') (Join-Path $auditRoot 'ContextNativeProbe.java')
    if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
    foreach ($item in @(
        @('Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf', 'dolphin'),
        @('Gemma-3-it-4B-Uncensored-D_AU-Q4_0.gguf', 'gemma3'),
        @('huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf', 'huihui')
    )) {
        & java "-Djava.library.path=$projectRoot\docs\validation\dialogue-0.7.4\native-final" -cp $classPath dev.vincent.geschichten.ai.ContextNativeProbe (Join-Path $files $item[0]) $cases (Join-Path $auditRoot ($item[1] + '-tokens-cache.json')) 1 'A-short,E-courtyard,overflow-character-budget'
        if ($LASTEXITCODE -ne 0) { throw "Native probe failed: $($item[1])" }
    }
} else {
    & javac -cp $classPath -d $auditRoot (Join-Path $auditRoot 'AuditLiteRT.java')
    if ($LASTEXITCODE -ne 0) { throw 'LiteRT probe compilation failed' }
    foreach ($item in @(
        @('gemma-4-E2B-it.litertlm', 'gemma4'),
        @('Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm', 'qwen25'),
        @('Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm', 'qwen06')
    )) {
        & java -Xmx5g -cp $classPath AuditLiteRT (Join-Path $files $item[0]) (Join-Path $auditRoot ('cache-' + $item[1])) $cases (Join-Path $auditRoot ($item[1] + '-tokens.json')) 1 'A-short,E-courtyard,overflow-character-budget' fixed
        if ($LASTEXITCODE -ne 0) { throw "LiteRT probe failed: $($item[1])" }
    }
}
