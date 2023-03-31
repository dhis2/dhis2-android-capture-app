#!/bin/bash
set -ex

VERSION_SDK=$(cat dependencies.gradle | grep "minSdk" | awk -F':' '{print $2}' | awk -F',' '{print $1}')
KITKAT_SDK=19

if [[ "$VERSION_SDK" -eq "KITKAT_SDK" ]];
then
  echo "Changing environment to Java 8 ..."
  sudo update-alternatives --set javac /usr/lib/jvm/java-8-openjdk-amd64/bin/javac
  sudo update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

  export JAVA_HOME='/usr/lib/jvm/java-8-openjdk-amd64'
  envman add --key JAVA_HOME --value '/usr/lib/jvm/java-8-openjdk-amd64'
fi