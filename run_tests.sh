#!/bin/bash
# This will make exit immediately if any command fails
set -e

AVD_NAME="Pixel 7 ANDROID 13"
APP_PACKAGE="com.dhis2.debug"
ANDROID_SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

echo "Running Ktlint check..."
./gradlew ktlintCheck

echo "Running Unit Tests..."
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest

# Check if the target AVD is already running
EMULATOR_SERIAL=""
AVD_NAME_NORMALIZED="${AVD_NAME// /_}"
for serial in $(adb devices 2>/dev/null | grep "^emulator" | awk '{print $1}'); do
    running_avd=$(adb -s "$serial" emu avd name 2>/dev/null | head -1 | tr -d '\r')
    if [[ "$running_avd" == "$AVD_NAME_NORMALIZED" ]]; then
        EMULATOR_SERIAL="$serial"
        break
    fi
done

if [[ -z "$EMULATOR_SERIAL" ]]; then
    # AVD not running — check if it exists before trying to start it
    if ! "${ANDROID_SDK}/emulator/emulator" -list-avds 2>/dev/null | grep -qF "${AVD_NAME}"; then
        echo ""
        echo "Skipping E2E tests — AVD '${AVD_NAME}' not found."
        echo "   To enable, create it in Android Studio:"
        echo "     Tools > Device Manager > Create Device"
        echo "     Select 'Pixel 7', choose API 33 (Android 13), name it '${AVD_NAME}'"
        echo ""
        echo "All tasks completed (E2E skipped)!"
        exit 0
    fi

    echo "Starting emulator '${AVD_NAME}'..."
    "${ANDROID_SDK}/emulator/emulator" -avd "${AVD_NAME}" -no-snapshot-load &
    echo "Waiting for emulator to boot..."
    adb wait-for-device
    until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
        sleep 3
    done
    EMULATOR_SERIAL=$(adb devices | grep "^emulator" | awk '{print $1}' | head -1)
    echo "Emulator ready: $EMULATOR_SERIAL"
fi

echo "Building test APKs..."
./gradlew :app:assembleDhis2Debug :app:assembleDhis2DebugAndroidTest

# Uninstall existing app if present
if adb -s "$EMULATOR_SERIAL" shell pm list packages 2>/dev/null | grep -q "^package:${APP_PACKAGE}$"; then
    echo "Uninstalling existing ${APP_PACKAGE}..."
    adb -s "$EMULATOR_SERIAL" uninstall "${APP_PACKAGE}"
fi

echo "Running E2E tests..."
./gradlew :app:connectedDhis2DebugAndroidTest

echo "All tasks completed!"
