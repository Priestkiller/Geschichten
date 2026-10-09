$ErrorActionPreference='Stop'
$projectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path
. (Join-Path (Split-Path $projectRoot) 'Build-Umgebung.ps1')
$sdkCmake=Join-Path $env:ANDROID_HOME 'cmake/3.22.1/bin'
$compilerRoot=(Join-Path (Split-Path $projectRoot) '.build-tools/native-probes/llvm-mingw-20260922-ucrt-x86_64/bin').Replace('\','/')
$env:PATH=$compilerRoot+';'+$sdkCmake+';'+$env:PATH
& "$sdkCmake/cmake.exe" -S "$PSScriptRoot/native-host" -B "$PSScriptRoot/native-cpu" -G Ninja "-DCMAKE_C_COMPILER=$compilerRoot/clang.exe" "-DCMAKE_CXX_COMPILER=$compilerRoot/clang++.exe" -DCMAKE_BUILD_TYPE=Release *> "$PSScriptRoot/native-config.log"
if($LASTEXITCODE -ne 0){throw 'Native configuration failed'}
& "$sdkCmake/cmake.exe" --build "$PSScriptRoot/native-cpu" --target geschichten_gguf -j 4 *> "$PSScriptRoot/native-build.log"
if($LASTEXITCODE -ne 0){throw 'Native build failed'}
Write-Output 'Host embedding probe built from pinned production runtime.'
