#!/bin/bash
# This will make exit immediately if any command fails
set -e

echo "Running full verification (ktlint + unit tests + coverage)..."
./gradlew verifyAll

echo "All tasks completed!"
