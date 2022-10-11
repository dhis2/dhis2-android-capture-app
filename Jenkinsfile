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
                    echo 'Running unit tests'
                    sh './gradlew testDhisDebugUnitTestCoverage'
                }
            }
        }
        stage('Sonnarqube') {
            environment {
                BITRISE_GIT_BRANCH = env.GIT_BRANCH
                BITRISEIO_GIT_BRANCH_DEST = "${env.CHANGE_TARGET == null ? env.GIT_BRANCH : env.CHANGE_TARGET}"
                BITRISE_PULL_REQUEST = env.CHANGE_ID
                SONAR_TOKEN = credentials('android-sonarcloud-token')
            }
            steps {
                script {
                    echo 'Running sonnarqube'
                    sh 'echo $BITRISEIO_GIT_BRANCH_DEST'
                    sh './gradlew sonarqube'
                }
            }
        }
        stage('Build Test APKs') {
            steps {
                script {
                    echo 'Building UI APKs'
                    sh './gradlew :app:assembleDhisUITestingDebug :app:assembleDhisUITestingDebugAndroidTest'
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
            slackSend channel: '#android-capture-app-ci', color: 'good', message: '*Build Succeeded*\n'+ custom_msg()
        }

        failure {
            slackSend channel: '#android-capture-app-ci', color:'bad', failOnError:true, message:'*Build Failed*\n'+ custom_msg()
        }
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

