#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if [ -z "${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}" ] && [ ! -f local.properties ]; then
    echo "Set ANDROID_HOME to your Android SDK (API 35), or create local.properties."
    exit 1
fi

./gradlew :app:assembleDebug --console=plain
cp app/build/outputs/apk/debug/app-debug.apk Geschichten-0.8.3.apk
echo "Created: $(pwd)/Geschichten-0.8.3.apk"
