#!/bin/sh

ADB="/Users/runner/Library/Android/sdk/platform-tools/adb"

function reset_emulator() {
	echo "Attempt to close any system dialogs"
	$ADB shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS

	echo "Send keystroke Arrow Right"
	sleep 3; $ADB shell input keyevent 22
	echo "Send keystroke Arrow Right again"
	sleep 3; $ADB shell input keyevent 22
	echo "Send keystroke Enter to press a button on the dialog"
	sleep 3; $ADB shell input keyevent 66

	echo "Lock orientation"
	$ADB shell settings put system accelerometer_rotation 1
}

echo "Build apk"
./gradlew :openmrs-client:assembleDebug

echo "Installing APK"
$ADB install openmrs-client/build/outputs/apk/debug/openmrs-client-debug.apk

reset_emulator

echo "Start screen recording"
$ADB emu screenrecord start --time-limit 360 maestro.webm

echo "Starting maestro"
maestro test --format=junit --output=report.xml --no-ansi .maestro