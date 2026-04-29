#!/bin/bash
set -ex
source config_jenkins.init
# shellcheck source=_bs_common.sh
source "$(dirname "${BASH_SOURCE[0]}")/_bs_common.sh"

bs_upload_apks "$BROWSERSTACK_USR" "$BROWSERSTACK_PSW" "$app_apk_path" "$test_apk_path"
app_url="$BS_APP_URL"
test_url="$BS_TEST_URL"

echo "Starting execution of espresso tests..."
shards=$(jq -n \
    --arg number_of_shards "$browserstack_number_of_parallel_executions" \
    '{numberOfShards: $number_of_shards}')

json=$(jq -n \
    --argjson app_url $app_url \
    --argjson test_url $test_url \
    --argjson devices ["$browserstack_device_list_landscape"] \
    --arg logs "$browserstack_device_logs" \
    --arg video "$browserstack_video" \
    --arg loc "$browserstack_local" \
    --arg locId "$browserstack_local_identifier" \
    --arg gpsLocation "$browserstack_gps_location" \
    --arg language "$browserstack_language" \
    --arg locale "$browserstack_locale" \
    --arg deviceLogs "$browserstack_deviceLogs" \
    --arg allowDeviceMockServer "$browserstack_allowDeviceMockServer" \
    --argjson shards "$shards" \
    --arg singleRunnerInvocation "$browserstack_singleRunnerInvocation" \
    --arg buildTag "$buildTag" \
    --arg deviceOrientation "$browserstack_deviceOrientation" \
    '{devices: $devices, app: $app_url, testSuite: $test_url, logs: $logs, video: $video, local: $loc, localIdentifier: $locId, gpsLocation: $gpsLocation, language: $language, locale: $locale, deviceLogs: $deviceLogs, allowDeviceMockServer: $allowDeviceMockServer, shards: $shards, singleRunnerInvocation: $singleRunnerInvocation, buildTag: $buildTag, deviceOrientation: $deviceOrientation}')

test_execution_response="$(curl -X POST https://api-cloud.browserstack.com/app-automate/espresso/v2/build \
    -d "$json" -H "Content-Type: application/json" -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW")"

build_id=$(echo "$test_execution_response" | jq -r .build_id)
echo "build id running: $build_id"

bs_poll_and_check "$BROWSERSTACK_USR" "$BROWSERSTACK_PSW" "$build_id" "$build_time_average_long" "$polling_interval"
