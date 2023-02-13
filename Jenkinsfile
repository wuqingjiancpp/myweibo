pipeline {
    agent any
    environment {
        JAVA_HOME='/var/jenkins_home/jdk17-1/jdk-17'
    }
    stages {
        stage('init'){
            steps {
                sh 'pwd'
                sh 'chmod +x gradlew'
                sh 'printenv'
            }

        }
        stage('Compile') {
            steps {
                gradlew('clean')
                gradlew('classes')
            }
        }
        stage('Unit Tests') {
            steps {
                gradlew('test')
            }
            post {
                always {
                    junit '**/build/test-results/test/TEST-*.xml'
                }
            }
        }
        stage('Assemble') {
            steps {
                gradlew('assemble')
                gradlew('publish')
                stash includes: '**/build/libs/*.jar', name: 'app'
            }
        }
        stage('Promotion') {
            steps {
                timeout(time: 1, unit:'DAYS') {
                    input 'Deploy to Production?'
                }
            }
        }
        stage('Deploy') {
            steps {
                sh 'docker build -t accendl/myweibo .'
                sh 'docker login'
                sh 'docker image push accendl/myweibo'
            }
        }
    }
}

def gradlew(String... args) {
    sh "./gradlew :web:${args.join(' ')} -s"
}