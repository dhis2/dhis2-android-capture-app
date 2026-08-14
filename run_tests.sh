#!/bin/bash
# This will make exit immediately if any command fails
set -e

echo "Running Ktlint check..."
./gradlew ktlintCheck

echo "Running Unit Tests..."
# The plugin-system modules do not answer to any of the Android test task names above:
# :plugin-sdk-gradle is a plain JVM (Gradle plugin) module, and :plugin-sdk runs its commonTest
# through the desktop target.
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest \
  :plugin-sdk-gradle:test :plugin-sdk:desktopTest

echo "All tasks completed!"
