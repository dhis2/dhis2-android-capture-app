#!/bin/bash
set -ex
source config.init

# Upload app and testing apk
echo "Uploading app APK to Browserstack..."
upload_app_response="$(curl -u $browserstack_username:$browserstack_access_key -X POST https://api-cloud.browserstack.com/app-automate/upload -F file=@$app_apk_path)"
app_url=$(echo "$upload_app_response" | jq .app_url)

echo "Uploading test APK to Browserstack..."
upload_test_response="$(curl -u $browserstack_username:$browserstack_access_key -X POST https://api-cloud.browserstack.com/app-automate/espresso/test-suite -F file=@$test_apk_path)"
test_url=$(echo "$upload_test_response" | jq .test_url)

# Prepare json and run tests
echo "Starting execution of espresso tests..."
json=$(jq -n \
                --argjson app_url $app_url \
                --argjson test_url $test_url \
                --argjson devices ["$browserstack_device_list"] \
                --argjson package ["$browserstack_package"] \
                --argjson class ["$browserstack_class"] \
                --argjson annotation ["$browserstack_annotation"] \
                --arg size "$browserstack_size" \
                --arg logs "$browserstack_device_logs" \
                --arg video "$browserstack_video" \
                --arg loc "$browserstack_local" \
                --arg locId "$browserstack_local_identifier" \
                --arg gpsLocation "$browserstack_gps_location" \
                --arg language "$browserstack_language" \
                --arg locale "$browserstack_locale" \
                --arg deviceLogs "$browserstack_deviceLogs" \
                '{devices: $devices, app: $app_url, testSuite: $test_url, package: $package, class: $class, annotation: $annotation, size: $size, logs: $logs, video: $video, local: $loc, localIdentifier: $locId, gpsLocation: $gpsLocation, language: $language, locale: $locale, deviceLogs: $deviceLogs}')

test_execution_response="$(curl -X POST https://api-cloud.browserstack.com/app-automate/espresso/build -d \ "$json" -H "Content-Type: application/json" -u "$browserstack_username:$browserstack_access_key")"

# Get build
build_id=$(echo "$test_execution_response" | jq -r .build_id)
echo "build id running: $build_id"

# Monitor build status
build_status="running"
sleep $build_time_average
echo "Monitoring build status started...."

while [[ $build_status = "running" ]];
do
  # Get build status
  build_status_response="$(curl -u "$browserstack_username:$browserstack_access_key" -X GET "https://api-cloud.browserstack.com/app-automate/espresso/builds/$build_id")"
  build_status=$(echo "$build_status_response" | jq -r .status)
  echo "current build status: $build_status"

  # Sleep until next poll
  sleep $polling_interval
done

# Retrieve device status and tests reports
build_status=$(echo "$build_status_response" | jq -r '.devices | to_entries[].value.status')
test_reports_url="https://app-automate.browserstack.com/dashboard/v2/builds/$build_id"

# Export test reports to bitrise
envman add --key BROWSERSTACK_TEST_REPORTS --value "$test_reports_url"

# Delegate final status to CI enviroment
if [[ $build_status = "passed" ]];
then
	echo "Browserstack build passed, please check the execution of your tests $test_reports_url"
	exit 0
else
	echo "Browserstack build failed, please check the execution of your tests $test_reports_url"
	exit 1
fi
