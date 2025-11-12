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
        disableConcurrentBuilds(abortPrevious: true)
        skipStagesAfterUnstable()
    }

    stages {
        stage('Check for [skip ci]') {
            when {
                expression {
                    return isSkipCI()
                }
            }
            steps {
                script {
                    currentBuild.result = 'UNSTABLE' // Mark build as a warning instead of an error
                    echo "⚠️ Warning: Skipping CI because '[skip ci]' was found in the PR title or description."
                }
            }
        }
        stage('Change to JAVA 17') {
            steps {
                script {
                    echo 'Changing JAVA version to 17'
                    sh 'sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java'
                    env.JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
                }
            }
        }
        stage('Check PR Size') {
            when {
                expression {
                    return !isSkipSizeCheck()
                }
            }
            environment {
                GIT_BRANCH = "${env.CHANGE_BRANCH}"
                GIT_BRANCH_DEST = "${env.CHANGE_TARGET == null ? '' : env.CHANGE_TARGET}"
            }
            steps {
                script {
                    echo "Checking PR Size against ${env.CHANGE_TARGET ?: 'None (CI build)'}"
                    sh 'chmod +x ./scripts/check_pr_size.sh'
                    sh './scripts/check_pr_size.sh'
                }
            }
        }
        stage('Lint Check') {
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
                    sh './gradlew testDebugUnitTest testDhis2DebugUnitTest --stacktrace --no-daemon'
                }
            }
        }
        stage('Build Test APKs') {
            steps {
                script {
                    echo 'Building UI APKs'
                    sh './gradlew :app:assembleDhis2Debug :app:assembleDhis2DebugAndroidTest :form:assembleAndroidTest'
                }
            }
        }
        stage('Run Form Tests') {
                environment {
                    BROWSERSTACK = credentials('android-browserstack')
                    form_apk = sh(returnStdout: true, script: 'find form/build/outputs -iname "*.apk" | sed -n 1p')
                    form_apk_path = "${env.WORKSPACE}/${form_apk}"
                    buildTag = "${env.GIT_BRANCH} - form"
                }
                steps {
                    dir("${env.WORKSPACE}/scripts"){
                        script {
                            echo 'Browserstack deployment and running Form module tests'
                            sh 'chmod +x browserstackJenkinsForm.sh'
                            sh './browserstackJenkinsForm.sh'
                        }
                    }
                }
            }
        stage('Run UI Tests in portrait') {
            environment {
                BROWSERSTACK = credentials('android-browserstack')
                app_apk = sh(returnStdout: true, script: 'find app/build/outputs/apk/dhis2/debug -iname "*.apk"')
                test_apk = sh(returnStdout: true, script: 'find app/build/outputs/apk/androidTest -iname "*.apk"')
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
        stage('Run UI Tests in Landscape') {
            environment {
                BROWSERSTACK = credentials('android-browserstack')
                app_apk = sh(returnStdout: true, script: 'find app/build/outputs/apk/dhis2/debug -iname "*.apk"')
                test_apk = sh(returnStdout: true, script: 'find app/build/outputs/apk/androidTest -iname "*.apk"')
                app_apk_path = "${env.WORKSPACE}/${app_apk}"
                test_apk_path = "${env.WORKSPACE}/${test_apk}"
                buildTag = "${env.GIT_BRANCH}"
            }
            steps {
                dir("${env.WORKSPACE}/scripts"){
                    script {
                        echo 'Browserstack deployment and running tests'
                        sh 'chmod +x browserstackJenkinsLandscape.sh'
                        sh './browserstackJenkinsLandscape.sh'
                    }
                }
            }
        }
        stage('JaCoCo report') {
            steps {
                script {
                    echo 'Running JaCoCo report on app module'
                    sh './gradlew jacocoReport --stacktrace --no-daemon'
                }
            }
        }
        stage('Sonarqube') {
            environment {
                GIT_BRANCH = "${env.GIT_BRANCH}"
                // Jenkinsfile considers empty value ('') as null
                GIT_BRANCH_DEST = "${env.CHANGE_TARGET == null ? '' : env.CHANGE_TARGET}"
                PULL_REQUEST = "${env.CHANGE_ID == null ? '' : env.CHANGE_ID }"
                SONAR_TOKEN = credentials('android-sonarcloud-token')
            }
            steps {
                script {
                    echo 'Running Sonarqube'
                    sh 'chmod +x ./scripts/sonarqube.sh'
                    sh './scripts/sonarqube.sh'
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

def isSkipCI() {
    def prTitle = env.CHANGE_TITLE ?: ""
    def prDescription = env.CHANGE_DESCRIPTION ?: ""
    return (prTitle.contains("[skip ci]") || prDescription.contains("[skip ci]"))
}

def isSkipSizeCheck() {
    def prTitle = env.CHANGE_TITLE ?: ""
    def prDescription = env.CHANGE_DESCRIPTION ?: ""
    return (prTitle.contains("[skip size]") || prDescription.contains("[skip size]"))
}
