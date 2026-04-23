pipeline {
  agent any

  tools {
    jdk 'jdk8'
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build + Test + Coverage') {
      steps {
        sh 'chmod +x mvnw'
        sh './mvnw --batch-mode --no-transfer-progress clean verify'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
          archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
        }
      }
    }

    stage('Docker Smoke Test') {
      when {
        branch 'main'
      }
      steps {
        sh 'docker build -t spring-thymeleaf-app:staging .'
        sh '''
          docker rm -f smoke || true
          docker run -d --name smoke -p 8080:8080 spring-thymeleaf-app:staging
          sleep 12
          curl -f http://localhost:8080
          docker rm -f smoke
        '''
      }
    }
  }
}
