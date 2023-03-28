#!/bin/bash
set -ex
source config_jenkins.init

# Upload app and testing apk
echo "Uploading app APK to Browserstack..."
upload_app_response="$(curl -u $BROWSERSTACK_USR:$BROWSERSTACK_PSW -X POST https://api-cloud.browserstack.com/app-automate/upload -F file=@$app_apk_path)"
app_url=$(echo "$upload_app_response" | jq .app_url)

echo "Uploading test APK to Browserstack..."
upload_test_response="$(curl -u $BROWSERSTACK_USR:$BROWSERSTACK_PSW -X POST https://api-cloud.browserstack.com/app-automate/espresso/test-suite -F file=@$test_apk_path)"
test_url=$(echo "$upload_test_response" | jq .test_url)

# Prepare json and run tests
echo "Starting execution of espresso tests..."
shards=$(jq -n \
                --arg number_of_shards "$browserstack_number_of_parallel_executions" \
                '{numberOfShards: $number_of_shards}')

json=$(jq -n \
                --argjson app_url $app_url \
                --argjson test_url $test_url \
                --argjson devices ["$browserstack_device_list"] \
                --argjson class ["$browserstack_class"] \
                --arg logs "$browserstack_device_logs" \
                --arg video "$browserstack_video" \
                --arg loc "$browserstack_local" \
                --arg locId "$browserstack_local_identifier" \
                --arg gpsLocation "$browserstack_gps_location" \
                --arg language "$browserstack_language" \
                --arg locale "$browserstack_locale" \
                --arg deviceLogs "$browserstack_deviceLogs" \
                --arg allowDeviceMockServer  "$browserstack_allowDeviceMockServer" \
                --argjson shards "$shards" \
                '{devices: $devices, app: $app_url, testSuite: $test_url, class: $class, logs: $logs, video: $video, local: $loc, localIdentifier: $locId, gpsLocation: $gpsLocation, language: $language, locale: $locale, deviceLogs: $deviceLogs, allowDeviceMockServer: $allowDeviceMockServer, shards: $shards}')

test_execution_response="$(curl -X POST https://api-cloud.browserstack.com/app-automate/espresso/v2/build -d \ "$json" -H "Content-Type: application/json" -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW")"

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
  build_status_response="$(curl -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW" -X GET "https://api-cloud.browserstack.com/app-automate/espresso/builds/$build_id")"
  build_status=$(echo "$build_status_response" | jq -r .status)
  echo "current build status: $build_status"

  # Sleep until next poll
  sleep $polling_interval
done

# Export test reports to bitrise
test_reports_url="https://app-automate.browserstack.com/dashboard/v2/builds/$build_id"

# weird behavior from Browserstack api, you can have "done" status with failed tests
# "devices" only show one device result which is inconsistance
# then "device_status" is checked
if [[ $build_status = "failed" || $build_status = "error" ]];
then
	echo "Browserstack build failed, please check the execution of your tests $test_reports_url"
  exit 1
else
  device_status=$(echo "$build_status_response" | jq -r '.device_statuses.error | to_entries[].value')
  if [[ $device_status = "Failed" ]]; # for this Failed Browserstack used bloq mayus
  then
	  echo "Browserstack build failed, please check the execution of your tests $test_reports_url"
    exit 1
  else
  	echo "Browserstack build passed, please check the execution of your tests $test_reports_url"
    exit 0
  fi
fi
