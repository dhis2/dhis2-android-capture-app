#!/bin/bash
set -ex

# Change waiting time to poll build status
sed -i -e 's/build_time_average=660/build_time_average=100/g' config.init

# Change test suite to execute flows instead of use cases
sed -i -e 's/org.dhis2.usescases.UseCaseTestsSuite/org.dhis2.usescases.FlowTestsSuite/g' config.init

#Change device to pixel 3a
sed -i -e 's/Samsung Galaxy S10/Google Pixel 3a/g' config.init
