#!/usr/bin/env python
import subprocess

# subprocess.call(".././gradlew --stacktrace app:connectedAndroidTest -P android.testInstrumentationRunnerArguments.class=org.dhis2.usecases.main.MainTest#copyDatabase", shell=True)
subprocess.call("adb pull sdcard/dhis_test.db ../app/src/dhisUITesting/assets/databases", shell=True)
