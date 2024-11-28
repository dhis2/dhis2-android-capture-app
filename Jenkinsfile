//Sets cron schedule just for PUSH job
String cron_string = JOB_NAME.startsWith('android-multibranch-PUSH') ? '0 0 * * *' : '0 0 0 0 0'

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
        stage('Print env variables') {
            steps {
                script {
                    echo "$cron_string"
                    echo cron_string
                    echo JOB_NAME
                    echo env.JOB_NAME
                    echo JOB_BASE_NAME
                    echo env.JOB_BASE_NAME
                }
            }
        }
    }
}
