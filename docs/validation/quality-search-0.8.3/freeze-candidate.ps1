$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$classes=Join-Path $PSScriptRoot 'candidate-classes'
if(Test-Path -LiteralPath $classes){throw 'Candidate already frozen'}
New-Item -ItemType Directory -Path $classes | Out-Null
Copy-Item -Path "$projectRoot/app/build/tmp/kotlin-classes/debug/*" -Destination $classes -Recurse
$oldTools=Join-Path $projectRoot 'docs/validation/dauerhaftes-gedaechtnis'
$cp=$PSScriptRoot+';'+$oldTools+';'+(Get-Content "$projectRoot/docs/validation/models-0.7.0/native-classpath.txt" -Raw).Trim().Replace((Join-Path $projectRoot 'app\build\tmp\kotlin-classes\debug'),$classes)
& javac -cp $cp -d $PSScriptRoot "$oldTools/HostGgufBridge.java" "$oldTools/MemoryModelProbe.java" "$PSScriptRoot/QualityProbe.java"
if($LASTEXITCODE -ne 0){throw 'Acceptance probe compile failed'}
Write-Output 'Candidate input planning frozen; common sampling retained. Official sampling showed no gain in 2 development controls.'
