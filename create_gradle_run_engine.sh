#!/usr/bin/env bash
# fail if any commands fails
set -e
# debug log
set -x

cd dhis2-rule-engine

FILE=build.gradle
if [ -f "$FILE" ]; then
    echo "$FILE rule engine exist"
    exit 0
fi

echo "apply plugin: 'java'

dependencies {

    testImplementation 'junit:junit:4.12'
    testCompile group: 'org.assertj', name: 'assertj-core', version: '3.5.2'
    testImplementation group: 'org.mockito', name: 'mockito-core', version: '2.2.9'
    testCompile 'com.google.truth:truth:0.30'
    testCompile group: 'nl.jqno.equalsverifier', name: 'equalsverifier', version: '2.1.6'

    implementation 'com.google.code.findbugs:jsr305:3.0.1'
    compileOnly 'com.google.auto.value:auto-value:1.3'
    annotationProcessor 'com.google.auto.value:auto-value:1.5.2'
    compile 'com.google.guava:guava:16+'
    implementation group: 'org.apache.commons', name: 'commons-lang3', version: '3.7'
    implementation group: 'commons-logging', name: 'commons-logging', version: '1.2'
    implementation group : 'org.apache.commons', name : 'commons-jexl', version: '2.1.1'
}" > build.gradle

echo "build gradle for rule engine generated"

cd ..

