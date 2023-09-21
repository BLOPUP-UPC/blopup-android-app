#!/bin/sh

ADB="/Users/runner/Library/Android/sdk/platform-tools/adb"

echo "Build apk"
./gradlew :openmrs-client:assembleDebug

echo "Installing APK"
$ADB install openmrs-client/build/outputs/apk/debug/openmrs-client-debug.apk

echo "Starting screenrecord"
$ADB emu screenrecord start --time-limit 460 maestro.webm

echo "Starting variables de entorno"
echo $MAESTRO_TEST_PASSWORD
echo $MAESTRO_TEST_USERNAME

echo "Starting maestro"
export MAESTRO_DRIVER_STARTUP_TIMEOUT=60000 # setting 60 seconds
maestro record -e USERNAME=testing.maestro -e PASSWORD=$MAESTRO_TEST_PASSWORD --no-ansi .maestro/e2e-register-patient.yaml