#!/bin/bash
# This will make exit immediately if any command fails
set -e

echo "Running conventions and harness checks..."
python3 scripts/check_conventions.py
# Harness drift may already be on the base branch: it only warns, so unrelated work is not blocked.
# Fix it when your change touches AGENTS.md, CLAUDE.md, .claude/ or scripts/check_*.py.
python3 scripts/check_harness.py || echo "WARNING: harness check failed (see above)."

echo "Running Ktlint check..."
./gradlew ktlintCheck

echo "Running Unit Tests..."
./gradlew testDebugUnitTest testDhis2DebugUnitTest testAndroidHostTest

echo "Running Desktop Build & Tests..."
./gradlew desktopTest

echo "All tasks completed!"
