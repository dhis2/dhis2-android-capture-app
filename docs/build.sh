#!/bin/bash


# perform all actions relative to the path of this script
SCRIPT_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$SCRIPT_DIR" ]]; then
  SCRIPT_DIR="$PWD"
else
  cd $SCRIPT_DIR
  SCRIPT_DIR="$PWD"
fi

# include helper functions
. "$SCRIPT_DIR/lib/doc_functions.sh"


# comment as you wish
# format:
#$> generate <doc name> <chapters subfolder> ["html","pdf","both"]

#echo "    - Android Capture App:" >> $myml
#generate "dhis2_android_capture_app"

echo "    - Implementation Guide:" >> $myml
generate "dhis2_android_implementation_guideline"  "android_implementation"

rm -rf $tmp
