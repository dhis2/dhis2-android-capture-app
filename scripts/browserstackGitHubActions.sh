#!/bin/bash
# browserstackGitHubActions.sh
# Uploads APKs to BrowserStack App Automate and runs Espresso tests.
# Mirrors the logic in browserstackJenkins.sh / browserstackJenkinsLandscape.sh /
# browserstackJenkinsForm.sh but is invoked from GitHub Actions instead of Jenkins.
#
# Usage:
#   ./browserstackGitHubActions.sh <suite>
#   suite: portrait | landscape | form
#
# Required environment variables (set by the workflow from GitHub Secrets):
#   BROWSERSTACK_USR        - BrowserStack username
#   BROWSERSTACK_PSW        - BrowserStack access key
#   buildTag                - Label shown in the BrowserStack dashboard
#
# Suite-specific env vars (resolved by the workflow step "Resolve APK paths"):
#   portrait/landscape: app_apk_path, test_apk_path
#   form:               form_apk_path

set -ex

# Variables sourced from config_jenkins.init:
#   build_time_average_short, build_time_average_long, polling_interval
#   browserstack_device_list, browserstack_device_list_landscape
#   browserstack_video, browserstack_local, browserstack_local_identifier
#   browserstack_gps_location, browserstack_language, browserstack_locale
#   browserstack_deviceLogs, browserstack_singleRunnerInvocation
#   browserstack_number_of_parallel_executions, browserstack_class
#   browserstack_allowDeviceMockServer, browserstack_deviceOrientation
source "$(dirname "$0")/config_jenkins.init"

SUITE="${1:-portrait}"

run_portrait_or_landscape() {
    local orientation="${1:-portrait}"

    echo "Uploading app APK to BrowserStack..."
    upload_app_response="$(curl -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" \
        -X POST https://api-cloud.browserstack.com/app-automate/upload \
        -F "file=@${app_apk_path}")" || { echo "Failed to upload app APK"; exit 1; }
    app_url=$(echo "$upload_app_response" | jq .app_url)

    echo "Uploading test APK to BrowserStack..."
    upload_test_response="$(curl -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" \
        -X POST https://api-cloud.browserstack.com/app-automate/espresso/test-suite \
        -F "file=@${test_apk_path}")" || { echo "Failed to upload test APK"; exit 1; }
    test_url=$(echo "$upload_test_response" | jq .test_url)

    echo "Starting Espresso test execution ($orientation)..."
    shards=$(jq -n \
        --arg number_of_shards "$browserstack_number_of_parallel_executions" \
        '{numberOfShards: $number_of_shards}')

    if [[ "$orientation" == "landscape" ]]; then
        device_list="[\"$browserstack_device_list_landscape\"]"
    else
        device_list="[\"$browserstack_device_list\"]"
    fi

    json=$(jq -n \
        --argjson app_url "$app_url" \
        --argjson test_url "$test_url" \
        --argjson devices "$device_list" \
        --argjson class "[\"$browserstack_class\"]" \
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
        --arg deviceOrientation "$orientation" \
        '{devices: $devices, app: $app_url, testSuite: $test_url, class: $class,
          logs: $logs, video: $video, local: $loc, localIdentifier: $locId,
          gpsLocation: $gpsLocation, language: $language, locale: $locale,
          deviceLogs: $deviceLogs, allowDeviceMockServer: $allowDeviceMockServer,
          shards: $shards, singleRunnerInvocation: $singleRunnerInvocation,
          buildTag: $buildTag, deviceOrientation: $deviceOrientation}')

    test_execution_response="$(curl -X POST \
        https://api-cloud.browserstack.com/app-automate/espresso/v2/build \
        -d "$json" -H "Content-Type: application/json" \
        -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW")" || { echo "Failed to start build"; exit 1; }

    monitor_and_exit "$test_execution_response" "$build_time_average_long"
}

run_form() {
    echo "Uploading form test APK to BrowserStack..."
    upload_form_apk_test_response="$(curl -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" \
        -X POST https://api-cloud.browserstack.com/app-automate/espresso/v2/module-app \
        -F "file=@${form_apk_path}")" || { echo "Failed to upload form APK"; exit 1; }
    module_url=$(echo "$upload_form_apk_test_response" | jq .module_url)

    echo "Starting module test execution..."
    json=$(jq -n \
        --argjson module_url "$module_url" \
        --argjson devices "[\"$browserstack_device_list\"]" \
        --arg video "$browserstack_video" \
        --arg deviceLogs "$browserstack_deviceLogs" \
        --arg singleRunnerInvocation "$browserstack_singleRunnerInvocation" \
        --arg buildTag "$buildTag" \
        '{devices: $devices, testSuite: $module_url, video: $video,
          deviceLogs: $deviceLogs, singleRunnerInvocation: $singleRunnerInvocation,
          buildTag: $buildTag}')

    test_execution_response="$(curl -X POST \
        https://api-cloud.browserstack.com/app-automate/espresso/v2/module-build \
        -d "$json" -H "Content-Type: application/json" \
        -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW")" || { echo "Failed to start module build"; exit 1; }

    monitor_and_exit "$test_execution_response" "$build_time_average_short"
}

monitor_and_exit() {
    local response="$1"
    local initial_wait="$2"

    build_id=$(echo "$response" | jq -r .build_id)
    echo "BrowserStack build id: $build_id"

    build_status="running"
    sleep "$initial_wait"
    echo "Monitoring build status..."

    while [[ $build_status == "running" ]]; do
        build_status_response="$(curl -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" \
            -X GET "https://api-cloud.browserstack.com/app-automate/espresso/builds/$build_id")" \
            || { echo "Failed to poll build status"; exit 1; }
        build_status=$(echo "$build_status_response" | jq -r .status)
        echo "current build status: $build_status"
        sleep "$polling_interval"
    done

    test_reports_url="https://app-automate.browserstack.com/dashboard/v2/builds/$build_id"

    if [[ $build_status == "failed" || $build_status == "error" ]]; then
        echo "BrowserStack build failed: $test_reports_url"
        exit 1
    else
        device_status=$(echo "$build_status_response" | jq -r '.device_statuses.error | to_entries[].value')
        if [[ $device_status == "Failed" ]]; then
            echo "BrowserStack build failed: $test_reports_url"
            exit 1
        else
            echo "BrowserStack build passed: $test_reports_url"
            exit 0
        fi
    fi
}

case "$SUITE" in
    portrait)  run_portrait_or_landscape "portrait" ;;
    landscape) run_portrait_or_landscape "landscape" ;;
    form)      run_form ;;
    *)
        echo "Unknown suite '$SUITE'. Valid options: portrait | landscape | form"
        exit 1
        ;;
esac
