#!/bin/sh

ADB="/Users/runner/Library/Android/sdk/platform-tools/adb"

echo "Build apk"
./gradlew :openmrs-client:assembleDebug

echo "Installing APK"
$ADB install openmrs-client/build/outputs/apk/debug/openmrs-client-debug.apk

echo "Starting maestro"
maestro record --format=junit --output=report.xml --no-ansi .maestro