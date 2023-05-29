pipeline {
    agent {
        label "ec2-android"
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '5'))
        timeout(time: 50)
    }

    stages{
        stage('Ktlint') {
            steps {
                script {
                    echo 'Running Ktlint'
                    sh './gradlew ktlintCheck'
                }
            }
        }
        stage('Unit tests') {
            environment {
                ANDROID_HOME = '/opt/android-sdk'
            }
            steps {
                script {
                    echo 'Running unit tests on App Module'
                    sh './gradlew testDhisDebugUnitTestCoverage'
                    echo 'Running unit tests on all other modules'
                    sh './gradlew testDebugUnitTest'
                }
            }
        }
        stage('Sonarqube') {
            environment {
                BITRISE_GIT_BRANCH = "$env.GIT_BRANCH"
                BITRISEIO_GIT_BRANCH_DEST = "${env.CHANGE_TARGET == null ? env.GIT_BRANCH : env.CHANGE_TARGET}"
                BITRISE_PULL_REQUEST = "${env.CHANGE_ID == null ? '' : env.CHANGE_ID}"
                SONAR_TOKEN = credentials('android-sonarcloud-token')
            }
            steps {
                script {
                    echo 'Running sonarqube'
                    sh 'echo $BITRISE_GIT_BRANCH'
                    sh 'echo $BITRISEIO_GIT_BRANCH_DEST'
                    sh 'echo $BITRISE_PULL_REQUEST'
                    sh './gradlew sonarqube'
                }
            }
        }
        stage('Build Test APKs') {
            steps {
                script {
                    echo 'Building UI APKs'
                    sh './gradlew :app:assembleDhisUITestingDebug :app:assembleDhisUITestingDebugAndroidTest :compose-table:assembleAndroidTest'
                }
            }
        }
        stage('Deploy compose-table module Tests') {
            environment {
                BROWSERSTACK = credentials('android-browserstack')
                compose_table_apk = sh(returnStdout: true, script: 'find compose-table/build/outputs -iname "*.apk" | sed -n 1p')
                compose_table_apk_path = "${env.WORKSPACE}/${compose_table_apk}"
            }
            steps {
                dir("${env.WORKSPACE}/scripts"){
                    script {
                        echo 'Browserstack deployment and running compose-table module tests'
                        sh 'chmod +x browserstackJenkinsCompose.sh'
                        sh './browserstackJenkinsCompose.sh'
                    }
                }
            }
        }
        stage('Deploy and Run UI Tests') {
            environment {
                BROWSERSTACK = credentials('android-browserstack')
                app_apk = sh(returnStdout: true, script: 'find app/build/outputs -iname "*.apk" | sed -n 1p')
                test_apk = sh(returnStdout: true, script: 'find app/build/outputs -iname "*.apk" | sed -n 2p')
                app_apk_path = "${env.WORKSPACE}/${app_apk}"
                test_apk_path = "${env.WORKSPACE}/${test_apk}"
            }
            steps {
                dir("${env.WORKSPACE}/scripts"){
                    script {
                        echo 'Browserstack deployment and running tests'
                        sh 'chmod +x browserstackJenkins.sh'
                        sh './browserstackJenkins.sh'
                    }
                }
            }
        }
    }
    post {
        success {
            sendNotification(env.GIT_BRANCH, '*Build Succeeded*\n', 'good')
        }

        failure {
            sendNotification(env.GIT_BRANCH, '*Build Failed*\n', 'bad')
        }
    }
}

def sendNotification(String branch, String messagePrefix, String color){
    if( !branch.startsWith('PR') ){
       slackSend channel: '#android-capture-app-ci', color: color, message: messagePrefix+ custom_msg()
   }
}

def custom_msg(){
  def BUILD_URL= env.BUILD_URL
  def JOB_NAME = env.JOB_NAME
  def BUILD_ID = env.BUILD_ID
  def BRANCH_NAME = env.GIT_BRANCH
  def JENKINS_LOG= "*Job:* $JOB_NAME\n *Branch:* $BRANCH_NAME\n *Build Number:* $BUILD_NUMBER (<${BUILD_URL}|Open>)"
  return JENKINS_LOG
}
