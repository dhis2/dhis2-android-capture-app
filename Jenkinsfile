//Sets cron schedule just for PUSH job
String cron_string = JOB_NAME.startsWith('android-multibranch-PUSH') ? '0 0 * * *' : ''

pipeline {
    agent {
        label "ec2-android"
    }

    triggers {
        cron(cron_string)
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '5'))
        timeout(time: 50)
        disableConcurrentBuilds(abortPrevious: true)
    }

    stages {
        stage('Change to JAVA 17') {
            steps {
                script {
                    echo 'Changing JAVA version to 17'
                    sh 'sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java'
                    env.JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
                }
            }
        }
        stage('Build Test APKs') {
            steps {
                script {
                    echo 'Building UI APKs'
                    sh './gradlew :app:assembleDhisUITestingDebug :app:assembleDhisUITestingDebugAndroidTest :compose-table:assembleAndroidTest :form:assembleAndroidTest'
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
                buildTag = "${env.GIT_BRANCH}"
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
