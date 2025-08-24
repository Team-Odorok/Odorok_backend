pipeline {
  agent any
  options {
    timestamps()
    // ansiColor('xterm')  ← 제거
  }

  environment {
    BRANCH     = 'test'
    IMAGE_NAME = 'odorok-backend'
    IMAGE_TAG  = 'latest'
    IMAGE      = "${IMAGE_NAME}:${IMAGE_TAG}"
    CONTAINER  = 'odorok-container'
    HOST_PORT  = '8080'
    APP_PORT   = '8080'
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: "${BRANCH}",
            credentialsId: 'GithubOdorok',
            url: 'https://github.com/Team-Odorok/Odorok_backend.git'
      }
    }

    stage('test') {
      environment {
        KAKAO_CLIENT_ID     = credentials('KAKAO_CLIENT_ID')
        KAKAO_CLIENT_SECRET = credentials('KAKAO_CLIENT_SECRET')
        JWT_SECRET          = credentials('JWT_SECRET')
        DURUNUBI_API_KEY    = credentials('DURUNUBI_API_KEY')
        KAKAO_REST_KEY      = credentials('KAKAO_REST_KEY')
        GPT_API_KEY         = credentials('GPT_API_KEY')
        AWS_ACCESS_KEY      = credentials('AWS_ACCESS_KEY')
        AWS_SECRET_KEY      = credentials('AWS_SECRET_KEY')
        // 필요 시 DB도 여기서:
        // DB_URL              = credentials('DB_URL')
        // DB_USERNAME         = credentials('DB_USERNAME')
        // DB_PASSWORD         = credentials('DB_PASSWORD')
      }
      steps {
        sh '''
          set -eu
          # 별도 export 불필요. 위 environment{}로 이미 ENV 주입됨.
          ./gradlew test --info
        '''
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
        sh 'docker build -t "${IMAGE}" .'
      }
    }

    stage('Stop & Remove Old Container') {
      steps {
        sh 'docker rm -f "${CONTAINER}" >/dev/null 2>&1 || true'
      }
    }

    stage('Run Container') {
      steps {
        withCredentials([
            string(credentialsId: 'KAKAO_CLIENT_ID', variable: 'KAKAO_CLIENT_ID'),
            string(credentialsId: 'KAKAO_CLIENT_SECRET', variable: 'KAKAO_CLIENT_SECRET'),
            string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
            string(credentialsId: 'KAKAO_REST_KEY', variable: 'KAKAO_REST_KEY'),
            string(credentialsId: 'GPT_API_KEY', variable: 'GPT_API_KEY'),
            string(credentialsId: 'AWS_ACCESS_KEY', variable: 'AWS_ACCESS_KEY'),
            string(credentialsId: 'AWS_SECRET_KEY', variable: 'AWS_SECRET_KEY'),
            string(credentialsId: 'DB_URL', variable: 'DB_URL'),
            string(credentialsId: 'DB_USERNAME', variable: 'DB_USERNAME'),
            string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')
        ]) {
          sh '''
            set -eu
            # 네트워크 보장 (없으면 생성)
            docker network inspect odorok-bridge >/dev/null 2>&1 || docker network create odorok-bridge

            # 기존 컨테이너 정리(혹시 남아있다면)
            docker rm -f "${CONTAINER}" >/dev/null 2>&1 || true

            # 자격증명은 환경에서 전달(-e KEY 형태면 현재 셸의 값이 그대로 전달됨)
            docker run -d \
            --name "${CONTAINER}" \
            -p ${HOST_PORT}:${APP_PORT} \
            --restart unless-stopped \
            --network odorok-bridge \
            -e SPRING_PROFILES_ACTIVE=docker \
            -e KAKAO_CLIENT_ID \
            -e KAKAO_CLIENT_SECRET \
            -e JWT_SECRET \
            -e KAKAO_REST_KEY \
            -e GPT_API_KEY \
            -e AWS_ACCESS_KEY \
            -e AWS_SECRET_KEY \
            -e DB_URL \
            -e DB_USERNAME \
            -e DB_PASSWORD \
            -e SPRING_DATASOURCE_URL="$DB_URL" \
            -e SPRING_DATASOURCE_USERNAME="$DB_USERNAME" \
            -e SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
            "${IMAGE}"
        '''
        }
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
