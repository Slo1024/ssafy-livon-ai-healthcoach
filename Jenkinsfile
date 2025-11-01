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
                    echo 'BE changes detected. Deploying backend services.'

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
                    def NGINX_CONFIG_DIR = IS_PROD ? 'LivOnInfra/nginx.prod' : 'LivOnInfra/nginx.dev'
                    def NGINX_CONFIG_SRC = IS_PROD ? 'LivOnInfra/nginx.prod/default.conf' : 'LivOnInfra/nginx.dev/default.conf'
                    def NGINX_CONFIG_DEST = "${NGINX_CONFIG_DIR}/default.conf"

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

                        echo "📁 Preparing Nginx config (${NGINX_CONFIG_SRC} -> ${NGINX_CONFIG_DEST})"
                        rm -rf ${NGINX_CONFIG_DIR}
                        mkdir -p ${NGINX_CONFIG_DIR}
                        touch ${NGINX_CONFIG_DEST}
                        cp -f ${NGINX_CONFIG_SRC} ${NGINX_CONFIG_DEST}
                        ls -l ${NGINX_CONFIG_DIR}

                        echo "🗑️ Removing existing Nginx container (${NGINX_CONTAINER}) if present..."
                        docker rm -f ${NGINX_CONTAINER} || true

                        echo "🌐 Running docker compose for Nginx (${COMPOSE_FILE})..."
                        docker compose -p ${PROJECT} -f ${COMPOSE_FILE} up -d --build nginx
                    """
                }
            }
        }
    }
}
