$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$cp=$PSScriptRoot+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),(Join-Path $PSScriptRoot 'baseline-classes'))
& javac -cp $cp -d $PSScriptRoot "$PSScriptRoot/FixtureMaterializer.java" "$PSScriptRoot/RecallExport.java"
if($LASTEXITCODE -ne 0){throw 'Materializer compile failed'}
if(!(Test-Path "$PSScriptRoot/materialized-scenes.json")){& java -cp $cp dev.vincent.geschichten.ai.FixtureMaterializer "$PSScriptRoot/new-scenes.json" "$PSScriptRoot/materialized-scenes.json"}
if($LASTEXITCODE -ne 0){throw 'Materializer failed'}
& java -cp $cp dev.vincent.geschichten.ai.RecallExport "$PSScriptRoot/materialized-scenes.json" "$PSScriptRoot/retrieval-development-inputs.json" development
if($LASTEXITCODE -ne 0){throw 'Recall export failed'}
