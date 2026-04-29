#!/bin/bash
# Sourced library — do not execute directly.

bs_upload_apks() {
    local user="$1" key="$2" app_apk="$3" test_apk="$4"
    echo "Uploading app APK to BrowserStack..."
    local resp
    resp="$(curl -u "${user}:${key}" -X POST \
        https://api-cloud.browserstack.com/app-automate/upload \
        -F "file=@${app_apk}")"
    BS_APP_URL=$(echo "$resp" | jq .app_url)

    echo "Uploading test APK to BrowserStack..."
    resp="$(curl -u "${user}:${key}" -X POST \
        https://api-cloud.browserstack.com/app-automate/espresso/test-suite \
        -F "file=@${test_apk}")"
    BS_TEST_URL=$(echo "$resp" | jq .test_url)
}

bs_poll_and_check() {
    local user="$1" key="$2" build_id="$3" initial_sleep="$4" interval="$5"
    local dashboard="https://app-automate.browserstack.com/dashboard/v2/builds/${build_id}"
    local status="running" resp

    echo "Build ID: ${build_id}"
    echo "Dashboard: ${dashboard}"

    sleep "$initial_sleep"
    echo "Polling build status..."
    while [[ "$status" == "running" ]]; do
        resp="$(curl -u "${user}:${key}" -X GET \
            "https://api-cloud.browserstack.com/app-automate/espresso/builds/${build_id}")"
        status=$(echo "$resp" | jq -r .status)
        echo "  status: $status"
        sleep "$interval"
    done

    if [[ "$status" == "failed" || "$status" == "error" ]]; then
        echo "FAILED – $dashboard"
        exit 1
    fi
    local device_status
    device_status=$(echo "$resp" | jq -r '.device_statuses.error | to_entries[].value')
    if [[ "$device_status" == "Failed" ]]; then
        echo "FAILED (device error) – $dashboard"
        exit 1
    fi
    echo "PASSED – $dashboard"
}
