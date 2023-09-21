#!/bin/sh

ADB="/Users/runner/Library/Android/sdk/platform-tools/adb"

echo "Build apk"
./gradlew :openmrs-client:assembleDebug

echo "Installing APK"
$ADB install openmrs-client/build/outputs/apk/debug/openmrs-client-debug.apk

echo "Starting screenrecord"
$ADB emu screenrecord start --time-limit 460 maestro.webm

echo "Starting maestro"
export MAESTRO_DRIVER_STARTUP_TIMEOUT=120000 # setting 120 seconds
maestro test -e MAESTRO_TEST_USERNAME=$MAESTRO_TEST_USERNAME -e MAESTRO_TEST_PASSWORD=$MAESTRO_TEST_PASSWORD --no-ansi .maestro/e2e-register-patient.yaml