@echo off
setlocal
cd /d "%~dp0"

rem Android Studio bundles a suitable Java runtime, including the compiler.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto java_ready
if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
    goto java_ready
)
echo Ein Java Development Kit wurde nicht gefunden (empfohlen: JDK 17 oder 21).
echo Bitte Android Studio installieren oder JAVA_HOME auf ein JDK setzen.
exit /b 1

:java_ready
if not defined ANDROID_HOME if defined ANDROID_SDK_ROOT set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
if not defined ANDROID_HOME if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
if not defined ANDROID_HOME (
    echo Android SDK wurde nicht gefunden.
    echo Bitte Android Studio einmal starten und im SDK Manager Android API 35 installieren.
    echo Alternativ ANDROID_HOME auf das vorhandene SDK setzen.
    exit /b 1
)

echo Geschichten wird gebaut. Beim ersten Mal werden die Bibliotheken heruntergeladen.
call gradlew.bat :app:assembleDebug --console=plain
if errorlevel 1 (
    echo.
    echo Der Build ist fehlgeschlagen. Die Fehlermeldung steht oberhalb.
    exit /b 1
)

copy /Y "app\build\outputs\apk\debug\app-debug.apk" "Geschichten-0.8.7.apk" >nul
if errorlevel 1 exit /b 1
echo.
echo Fertig: %CD%\Geschichten-0.8.7.apk
echo Diese Test-APK ist fuer ein Android-Handy mit ARM64 und Android 12 oder neuer.
exit /b 0
