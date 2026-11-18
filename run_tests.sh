#!/bin/bash
# This will make exit immediately if any command fails
set -e

echo "Running Ktlint check..."
./gradlew ktlintCheck

echo "Running Unit Tests..."
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest

echo "Running Desktop Build & Tests..."
./gradlew desktopTest

echo "All tasks completed!"
