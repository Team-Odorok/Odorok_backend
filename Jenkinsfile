pipeline {
  agent any
  options {
    timestamps()
    ansiColor('xterm')
  }
  environment {
    BRANCH        = 'test'
    IMAGE_NAME    = 'odorok-backend'
    IMAGE_TAG     = 'latest'
    IMAGE         = "${IMAGE_NAME}:${IMAGE_TAG}"
    CONTAINER     = 'odorok-container'
    HOST_PORT     = '8080'        // 호스트에서 열 포트
    APP_PORT      = '8080'        // 컨테이너 내부 앱 포트
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: "${BRANCH}",
            credentialsId: 'GithubOdorok',
            url: 'https://github.com/Team-Odorok/Odorok_backend.git'
      }
    }

    stage('Build (Gradle)') {
      steps {
        sh '''
          set -e
          chmod +x ./gradlew
          ./gradlew clean build -x test
        '''
      }
    }

    stage('Docker Build') {
      steps {
        sh '''
          set -e
          docker build -t "${IMAGE}" .
        '''
      }
    }

    stage('Stop & Remove Old Container') {
      steps {
        sh '''
          # 기존 컨테이너 있으면 강제 종료/삭제 (없어도 에러 안나게)
          docker rm -f "${CONTAINER}" >/dev/null 2>&1 || true
        '''
      }
    }

    stage('Run Container') {
      steps {
        sh '''
          set -e
          docker run -d \
            --name "${CONTAINER}" \
            -p ${HOST_PORT}:${APP_PORT} \
            --restart unless-stopped \
            "${IMAGE}"
        '''
      }
    }

    stage('Health Check') {
      steps {
        sh '''
          # 간단 헬스체크 (필요시 경로 수정)
          for i in {1..20}; do
            if curl -fsS "http://127.0.0.1:${HOST_PORT}/actuator/health" >/dev/null 2>&1; then
              echo "Health OK"
              exit 0
            fi
            echo "Waiting for app... ($i)"
            sleep 2
          done
          echo "Health check failed"
          exit 1
        '''
      }
    }
  }

  post {
    failure {
      echo 'Build failed. Showing last docker logs (if container exists).'
      sh 'docker logs --tail=200 "${CONTAINER}" || true'
    }
    always {
      archiveArtifacts artifacts: 'build/libs/*.jar', onlyIfSuccessful: false
    }
  }
}