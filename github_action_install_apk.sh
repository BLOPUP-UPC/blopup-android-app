#!/bin/sh

ADB="/Users/runner/Library/Android/sdk/platform-tools/adb"

echo "Build apk"
./gradlew :openmrs-client:assembleDebug

echo "Installing APK"
$ADB install openmrs-client/build/outputs/apk/debug/openmrs-client-debug.apk

echo "Start screen recording"
$ADB emu screenrecord start --time-limit 10 maestro.webm

echo "Starting maestro"
maestro test --format=junit --output=report.xml --no-ansi .maestro