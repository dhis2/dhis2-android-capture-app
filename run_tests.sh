#!/bin/bash

echo "Running Ktlint check..."
./gradlew ktlintCheck

echo "Running app module Unit Tests..."
./gradlew :app:testDebugUnitTest

echo "Running all modules Unit Tests..."
./gradlew testDebugUnitTest

echo "All tasks completed!"
