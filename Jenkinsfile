pipeline {
    agent any

    environment {
        BRANCH_NAME = "${env.GIT_BRANCH}".replaceAll(".*/", "")
    }

    stages {
        // 코드 체크아웃 (필수)
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // BE / FE 순차 배포
        // --- BE 배포 스테이지 (먼저 실행) ---
        stage('Deploy BE') {
            // when: 'LivOnBack/' 경로에 변경 사항이 있을 때만 이 스테이지를 실행
            when {
                anyOf {
                    changeset pattern: 'LivOnBack/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    echo "✅ BE 디렉토리 변경 감지 → 배포 시작"
                    
                    def IS_PROD = BRANCH_NAME == 'master'
                    def COMPOSE_FILE = IS_PROD ? 'LivOnInfra/docker-compose.prod.yml' : 'LivOnInfra/docker-compose.dev.yml'
                    def PROPERTIES_ID = IS_PROD ? 'yml-prod' : 'yml-dev'
                    def CONTAINER = IS_PROD ? 'livon-be-prod' : 'livon-be-dev'
                    
                    // application.yml 파일 주입
                    withCredentials([
                        file(credentialsId: PROPERTIES_ID, variable: 'APP_PROPS_FILE'),
                        // file(credentialsId: 'gcp-key', variable: 'GCP_KEY_FILE')
                    ]) {
                        dir('LivOnBack') {
                            sh """
                                echo "📦 application.yml 복사 중..."
                                rm -f application.yml
                                cp -f "$APP_PROPS_FILE" application.yml
                            """
                        }

                        // ... (주석 처리된 GCP 키 복사 로직) ...

                        // Docker Compose 실행
                        sh """
                            echo "🗑️ 기존 BE 컨테이너 삭제 (${CONTAINER})..."
                            docker rm -f ${CONTAINER} || true

                            echo "🚀 도커 컴포즈로 빌드 및 실행..."
                            docker compose -f ${COMPOSE_FILE} up -d --build livon-be
                        """
                    }
                }
            }
        } // End stage('Deploy BE')

        // --- FE 배포 스테이지 (BE 실행 후 실행) ---
        stage('Deploy FE') {
            // when: 'LivOnFront/web/' 경로에 변경 사항이 있을 때만 이 스테이지를 실행
            when {
                anyOf {
                    changeset pattern: 'LivOnFront/web/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    echo "✅ FE 디렉토리 변경 감지 → 배포 시작"
                    
                    def IS_PROD = BRANCH_NAME == 'master'
                    def COMPOSE_FILE = IS_PROD ? 'LivOnInfra/docker-compose.prod.yml' : 'LivOnInfra/docker-compose.dev.yml'
                    def ENV_ID = IS_PROD ? 'frontend-env-prod' : 'frontend-env-dev'
                    def CONTAINER = IS_PROD ? 'livon-fe-prod' : 'livon-fe-dev'

                    // .env 파일 주입
                    withCredentials([file(credentialsId: ENV_ID, variable: 'ENV_FILE')]) {
                        dir('LivOnFront/web') {
                            sh """
                                echo "🔒 .env 파일 복사 중..."
                                rm -f .env
                                cp "$ENV_FILE" .env
                            """
                        }
                    }

                    // Docker Compose 실행
                    sh """
                        echo "🗑️ 기존 FE 컨테이너 직접 삭제 (${CONTAINER})..."
                        docker rm -f ${CONTAINER} || true

                        echo "🚀 FE docker-compose 실행 중 (${COMPOSE_FILE})..."
                        docker compose -f ${COMPOSE_FILE} up -d --build livon-fe
                    """
                }
            }
        } // End stage('Deploy FE')
                
    } // End stages
}