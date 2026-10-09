$ErrorActionPreference = 'Stop'
. '..\Build-Umgebung.ps1'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug --no-daemon -PupdateRepository=Priestkiller/Geschichten -PupdateIncludePrerelease=true *> 'docs\validation\continuity-0.7.2\final-build-tests.log'
if ($LASTEXITCODE -ne 0) { throw 'Build or unit checks failed.' }
Copy-Item -LiteralPath 'app\build\outputs\apk\debug\app-debug.apk' -Destination 'docs\validation\continuity-0.7.2\production-final.apk'
Copy-Item -LiteralPath 'app\build\test-results\testDebugUnitTest' -Destination 'docs\validation\continuity-0.7.2\final-jvm-results' -Recurse
Copy-Item -LiteralPath 'app\build\reports\lint-results-debug.xml' -Destination 'docs\validation\continuity-0.7.2\lint-results-debug.xml'
Write-Output 'BUILD_CHECKS_PASSED'
$continuityClassPath = (Get-Content 'docs\validation\models-0.7.0\native-classpath.txt' -Raw).Trim()
$probeClassPath = 'docs\validation\continuity-0.7.2;' + $continuityClassPath
javac -encoding UTF-8 -cp $continuityClassPath -d 'docs\validation\continuity-0.7.2' 'docs\validation\continuity-0.7.2\ExportContinuityCases.java' 'docs\validation\continuity-0.7.2\LiteRTContinuityProbe.java' 'docs\validation\continuity-0.7.2\GgufContinuityProbe.java'
if ($LASTEXITCODE -ne 0) { throw 'Native probe compilation failed.' }
java -cp $probeClassPath ExportContinuityCases 'docs\validation\continuity-0.7.2\final-prompts.json'
if ($LASTEXITCODE -ne 0) { throw 'Prompt export failed.' }
$nativeCases = Get-Content 'docs\validation\continuity-0.7.2\final-prompts.json' -Raw | ConvertFrom-Json
[System.IO.File]::WriteAllText((Join-Path (Get-Location) 'docs\validation\continuity-0.7.2\final-grask-single.json'), '[' + ($nativeCases[0] | ConvertTo-Json -Depth 8) + ']', [System.Text.UTF8Encoding]::new($false))
java -Xmx2g -cp $probeClassPath LiteRTContinuityProbe 'docs\validation\models-0.7.0\model-files\gemma-4-E2B-it.litertlm' 'docs\validation\continuity-0.7.2\cache-gemma' 'docs\validation\continuity-0.7.2\final-prompts.json' 'docs\validation\continuity-0.7.2\final-gemma-answers.json' *> 'docs\validation\continuity-0.7.2\final-gemma-native.log'
if ($LASTEXITCODE -ne 0) { throw 'Gemma probe failed.' }
Write-Output 'GEMMA_CASES_COMPLETED'
foreach ($modelName in @('Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm', 'Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm')) {
    $shortName = if ($modelName.StartsWith('Qwen2.5')) { 'qwen25' } else { 'qwen3' }
    java -Xmx2g -cp $probeClassPath LiteRTContinuityProbe ('docs\validation\models-0.7.0\model-files\' + $modelName) ('docs\validation\continuity-0.7.2\cache-' + $shortName) 'docs\validation\continuity-0.7.2\final-grask-single.json' ('docs\validation\continuity-0.7.2\final-' + $shortName + '-answer.json') 256 *> ('docs\validation\continuity-0.7.2\final-' + $shortName + '-native.log')
    if ($LASTEXITCODE -ne 0) { throw ('Native probe failed: ' + $shortName) }
    Write-Output ('COMPLETED: ' + $shortName)
}
foreach ($modelName in @('Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf', 'huihui-qwen3-4b-instruct-2507-abliterated-q4_k_m.gguf', 'Gemma-3-it-4B-Uncensored-D_AU-Q4_0.gguf')) {
    $shortName = if ($modelName.StartsWith('Dolphin')) { 'dolphin' } elseif ($modelName.StartsWith('huihui')) { 'huihui' } else { 'gemma3' }
    java -Xmx1g '-Djava.library.path=docs\validation\models-0.7.0\native-host\build-fixed' -cp $probeClassPath GgufContinuityProbe ('docs\validation\models-0.7.0\model-files\' + $modelName) 'docs\validation\continuity-0.7.2\final-grask-single.json' ('docs\validation\continuity-0.7.2\final-' + $shortName + '-answer.json') *> ('docs\validation\continuity-0.7.2\final-' + $shortName + '-native.log')
    if ($LASTEXITCODE -ne 0) { throw ('Native probe failed: ' + $shortName) }
    Write-Output ('COMPLETED: ' + $shortName)
}
Write-Output 'ALL_SIX_MODELS_COMPLETED'
