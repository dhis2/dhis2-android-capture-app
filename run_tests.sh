#!/bin/bash
# This will make exit immediately if any command fails
set -e

echo "Running Ktlint check..."
./gradlew ktlintCheck

echo "Running app module Unit Tests..."
./gradlew :app:testDhis2DebugUnitTest

echo "Running all modules Unit Tests..."
./gradlew testDebugUnitTest

echo "All tasks completed!"
