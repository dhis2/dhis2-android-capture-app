#!/bin/bash
# Fails if any document teaches a source set or Gradle task that does not exist here.
#
# Lines that DENY the names are allowed -- SKILL.md has to be able to say
# "<name> does not exist" without tripping this check. Everything else is a hit.
#
# This script lives in .sh deliberately: the scan covers .md/.kts/.yml, so keeping
# the patterns here stops the gate from matching its own source.
set -uo pipefail
cd "$(dirname "$0")/../../.."

hits=$(grep -rn "androidUnitTest\|androidInstrumentedTest\|testAndroidDebugUnitTest" \
        --include="*.md" --include="*.kts" --include="*.yml" . \
        | grep -v "/build/" \
        | grep -vE "does not exist|not a task" || true)

if [ -n "$hits" ]; then
    echo "Stale source-set or task names still taught as guidance:"
    echo "$hits"
    echo
    echo "Use: androidHostTest/ (not androidUnitTest/), src/androidTest/ (not"
    echo "androidInstrumentedTest/), testAndroidHostTest (not testAndroidDebugUnitTest)."
    exit 1
fi
echo "OK: no document teaches a nonexistent source set or task."
