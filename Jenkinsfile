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

        stage('Publish APK') {
            when {
                anyOf {
                    changeset pattern: 'LivOnFront/mobile/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    echo '✅Mobile changes detected. Building and publishing APK.'

                    def IS_PROD = BRANCH_NAME == 'master'
                    def VARIANT = IS_PROD ? 'Release' : 'Debug'
                    def SUFFIX = VARIANT.toLowerCase()
                    def SAFE_BRANCH = (IS_PROD ? 'prod' : BRANCH_NAME.replaceAll('[^A-Za-z0-9-]', '-'))
                    def APK_NAME = "livon-${SAFE_BRANCH}-${env.BUILD_NUMBER}.apk"
                    def COMPOSE_FILE = IS_PROD ? 'LivOnInfra/docker-compose.prod.yml' : 'LivOnInfra/docker-compose.dev.yml'
                    def PROJECT = IS_PROD ? 'livon-prod' : 'livon-dev'

                    env.LIVON_APK_VARIANT = VARIANT
                    env.LIVON_APK_SUFFIX = SUFFIX
                    env.LIVON_APK_NAME = APK_NAME
                    env.LIVON_COMPOSE_FILE = COMPOSE_FILE
                    env.LIVON_PROJECT = PROJECT
                }

                dir('LivOnFront/mobile') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew clean assemble${LIVON_APK_VARIANT}
                    '''
                }

                sh '''
                    mkdir -p LivOnInfra/downloads
                    SOURCE_APK="LivOnFront/mobile/app/build/outputs/apk/${LIVON_APK_SUFFIX}/app-${LIVON_APK_SUFFIX}.apk"
                    if [ ! -f "$SOURCE_APK" ]; then
                        echo "APK not found at $SOURCE_APK"
                        exit 1
                    fi
                    cp "$SOURCE_APK" "LivOnInfra/downloads/${LIVON_APK_NAME}"
                    cp "$SOURCE_APK" "LivOnInfra/downloads/livon-${LIVON_APK_SUFFIX}-latest.apk"
                '''

                sh '''
                    docker compose -p ${LIVON_PROJECT} -f ${LIVON_COMPOSE_FILE} up -d nginx
                '''
            }
        }
    }
}
