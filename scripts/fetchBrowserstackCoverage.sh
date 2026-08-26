#!/bin/bash
#
# Downloads JaCoCo execution data (.ec) produced by an Espresso build on BrowserStack.
#
# BrowserStack runs the instrumented tests on its own devices, so the .ec files are
# written there and never reach the workspace on their own. Without this step
# jacocoReport finds an empty build/outputs/code_coverage tree and the reported
# coverage reflects unit tests only.
#
# Requires "coverage": true in the build request (see browserstackJenkins*.sh) and
# enableAndroidTestCoverage on the debug build type.
#
# Usage: ./fetchBrowserstackCoverage.sh <build_id> <label>
# Writes: <repo-root>/browserstack-coverage/<label>/<session_id>.ec
#
# Deliberately never fails the calling job: missing coverage should not turn a green
# test run red. It reports what it did and exits 0.

build_id="$1"
label="$2"

if [[ -z "$build_id" || -z "$label" ]]; then
    echo "fetchBrowserstackCoverage: usage: $0 <build_id> <label>" >&2
    exit 0
fi

if [[ -z "$BROWSERSTACK_USR" || -z "$BROWSERSTACK_PSW" ]]; then
    echo "fetchBrowserstackCoverage: BrowserStack credentials not set, skipping." >&2
    exit 0
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
out_dir="${repo_root}/browserstack-coverage/${label}"
mkdir -p "$out_dir"

api="https://api-cloud.browserstack.com/app-automate/espresso/v2/builds"

build_json="$(curl -sS -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" -X GET "${api}/${build_id}")"

# Sessions are nested one level below devices; sharding gives a device more than one.
session_ids=$(echo "$build_json" | jq -r '[.devices[]?.sessions[]?.id] | .[]' 2>/dev/null)

if [[ -z "$session_ids" ]]; then
    echo "fetchBrowserstackCoverage: no sessions found for build ${build_id}." >&2
    echo "$build_json" | head -c 400 >&2
    exit 0
fi

downloaded=0
for session_id in $session_ids; do
    target="${out_dir}/${session_id}.ec"
    http_code=$(curl -sS -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" \
        -X GET "${api}/${build_id}/sessions/${session_id}/coverage" \
        -o "$target" -w "%{http_code}")

    # The endpoint answers with the binary .ec; anything else leaves a junk file behind.
    if [[ "$http_code" == "200" && -s "$target" ]]; then
        downloaded=$((downloaded + 1))
        echo "fetchBrowserstackCoverage: session ${session_id} -> $(wc -c < "$target") bytes"
    else
        echo "fetchBrowserstackCoverage: session ${session_id} returned HTTP ${http_code}, skipping." >&2
        rm -f "$target"
    fi
done

echo "fetchBrowserstackCoverage: ${downloaded} coverage file(s) in ${out_dir}"
exit 0
