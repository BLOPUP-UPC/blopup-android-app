#!/bin/sh

ADB="/Users/runner/Library/Android/sdk/platform-tools/adb"

echo "Build apk"
./gradlew :openmrs-client:assembleDebug

echo "Installing APK"
$ADB install openmrs-client/build/outputs/apk/debug/openmrs-client-debug.apk

echo "Starting maestro recording "
maestro record --no-ansi .maestro/e2e-register-patient.yaml

echo "Starting maestro test execution "
maestro run --no-ansi .maestro/e2e-register-patient.yaml