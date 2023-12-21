#!/bin/bash
set -ex
source config_jenkins.init


echo "Uploading form test APK to Browserstack..."
upload_form_apk_test_response="$(curl -u $BROWSERSTACK_USR:$BROWSERSTACK_PSW -X POST https://api-cloud.browserstack.com/app-automate/espresso/v2/module-app -F file=@$form_apk_path)"
module_url=$(echo "$upload_form_apk_test_response" | jq .module_url)


# Prepare json and run tests
echo "Starting execution of tests..."
json=$(jq -n \
                --argjson module_url $module_url \
                --argjson devices ["$browserstack_device_list"] \
                --arg video "$browserstack_video" \
                --arg deviceLogs "$browserstack_deviceLogs" \
                '{devices: $devices, testSuite: $module_url, video: $video, deviceLogs: $deviceLogs'})

test_execution_response="$(curl -X POST https://api-cloud.browserstack.com/app-automate/espresso/v2/module-build -d \ "$json" -H "Content-Type: application/json" -u "$BROWSERSTACK_USR:$BROWSERSTACK_PSW")"

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
