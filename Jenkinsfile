pipeline {
    agent any

    environment {
        BRANCH_NAME = "${env.GIT_BRANCH}".replaceAll(".*/", "")
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Deploy BE') {
            when {
                anyOf {
                    changeset pattern: 'LivOnBack/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    echo '✅BE changes detected. Deploying backend services.'

                    def IS_PROD = BRANCH_NAME == 'master'
                    def COMPOSE_FILE = IS_PROD ? 'LivOnInfra/docker-compose.prod.yml' : 'LivOnInfra/docker-compose.dev.yml'
                    def PROPERTIES_ID = IS_PROD ? 'yml-prod' : 'yml-dev'
                    def CONTAINER = IS_PROD ? 'livon-be-prod' : 'livon-be-dev'
                    def PROJECT = IS_PROD ? 'livon-prod' : 'livon-dev'

                    withCredentials([file(credentialsId: PROPERTIES_ID, variable: 'APP_PROPS_FILE')]) {
                        dir('LivOnBack') {
                            sh '''
                                echo "📦 Copying application.yml..."
                                rm -f application.yml
                                cp -f "$APP_PROPS_FILE" application.yml
                            '''
                        }

                        sh """
                            echo "🗑️ Removing existing BE container (${CONTAINER}) if present..."
                            docker rm -f ${CONTAINER} || true

                            echo "🚀 Running docker compose for BE (${COMPOSE_FILE})..."
                            docker compose --project-directory LivOnInfra -p ${PROJECT} -f ${COMPOSE_FILE} up -d --build livon-be
                        """
                    }
                }
            }
        }

        stage('Deploy FE') {
            when {
                anyOf {
                    changeset pattern: 'LivOnFront/web/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    echo '✅FE changes detected. Deploying frontend and nginx.'

                    def IS_PROD = BRANCH_NAME == 'master'
                    def COMPOSE_FILE = IS_PROD ? 'LivOnInfra/docker-compose.prod.yml' : 'LivOnInfra/docker-compose.dev.yml'
                    def ENV_ID = IS_PROD ? 'frontend-env-prod' : 'frontend-env-dev'
                    def CONTAINER = IS_PROD ? 'livon-fe-prod' : 'livon-fe-dev'
                    def PROJECT = IS_PROD ? 'livon-prod' : 'livon-dev'
                    def NGINX_CONTAINER = IS_PROD ? 'nginx-prod' : 'nginx-dev'

                    withCredentials([file(credentialsId: ENV_ID, variable: 'ENV_FILE')]) {
                        dir('LivOnFront/web') {
                            sh '''
                                echo "🔒 Copying .env..."
                                rm -f .env
                                cp "$ENV_FILE" .env
                            '''
                        }
                    }

                    sh """
                        echo "🗑️ Removing existing FE container (${CONTAINER}) if present..."
                        docker rm -f ${CONTAINER} || true

                        echo "🚀 Running docker compose for FE (${COMPOSE_FILE})..."
                        docker compose -p ${PROJECT} -f ${COMPOSE_FILE} up -d --build livon-fe

                        echo "🗑️ Removing existing Nginx container (${NGINX_CONTAINER}) if present..."
                        docker rm -f ${NGINX_CONTAINER} || true

                        echo "🌐 Running docker compose for Nginx (${COMPOSE_FILE})..."
                        docker compose -p ${PROJECT} -f ${COMPOSE_FILE} up -d --build nginx
                    """
                }
            }
        }

        stage('Build Mobile APK') {
            when {
                changeset pattern: 'LivOnFront/mobile/**', comparator: 'ANT'
            }
            agent {
                docker {
                    // 안드로이드 SDK와 JDK 17이 설치된 이미지 사용
                    image 'reactivecircus/android-sdk:android-34-jdk17'
                    // (중요!) 이 컨테이너에도 'apk_storage' 볼륨을 연결해야 합니다.
                    args '-v apk_storage:/var/apk_storage' 
                }
            }
            steps {
                script {
                    echo "✅Mobile changes detected. Building APK for branch ${BRANCH_NAME}."
                    
                    // 1. 모바일 프로젝트 폴더로 이동
                    dir('LivOnFront/mobile') {
                        
                        // 2. gradlew 스크립트에 실행 권한 부여
                        // (빌드 스크립트를 실행 가능하게 만듭니다)
                        sh 'chmod +x ./gradlew'
                        
                        // 3. Gradle을 사용해 APK 빌드 (Debug 빌드 예시)
                        // 'assembleRelease'를 사용할 수도 있습니다.
                        echo 'Starting Gradle build...'
                        sh './gradlew assembleDebug' 
                        
                        // 4. 빌드된 APK 파일을 공유 볼륨으로 복사
                        // (주의!) 안드로이드 프로젝트 설정에 따라 이 경로는 다를 수 있습니다.
                        // 보통 'app/build/outputs/apk/debug/app-debug.apk' 입니다.
                        echo 'Copying APK to shared volume...'
                        sh 'cp app/build/outputs/apk/debug/app-debug.apk /var/apk_storage/livon-${BRANCH_NAME}-build-${BUILD_NUMBER}.apk'
                        
                        echo "APK successfully built and copied."
                        echo "Download at: /download/livon-${BRANCH_NAME}-build-${BUILD_NUMBER}.apk"
                    }
                }
            }
        }
    }
}
