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

        /* =========================
         *  Mobile APK 빌드
         * ========================= */
        stage('Build Mobile APK') {
            when {
                anyOf {
                    changeset pattern: 'LivOnFront/mobile/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    echo '📱 Mobile changes detected. Building APK...'

                    def IS_PROD = BRANCH_NAME == 'master'
                    def TASK    = IS_PROD ? 'assembleRelease' : 'assembleDebug'

                    dir('LivOnFront/mobile') {
                        sh """
                            chmod +x ./gradlew || true
                        """
                        sh "./gradlew clean ${TASK}"
                    }

                    // Jenkins 내 아티팩트 보관(선택)
                    archiveArtifacts artifacts: 'LivOnFront/mobile/**/build/outputs/apk/**/*.apk', fingerprint: true
                }
            }
        }

        /* =========================
         *  APK 공개(/download)
         * ========================= */
        stage('Publish APK to /download') {
            when {
                anyOf {
                    changeset pattern: 'LivOnFront/mobile/**', comparator: 'ANT'
                }
            }
            steps {
                script {
                    def IS_PROD = BRANCH_NAME == 'master'
                    def BASEURL = IS_PROD ? 'https://k13s406.p.ssafy.io' : 'https://k13s406.p.ssafy.io:8443'

                    // 최신 산출물 1개
                    def apk = sh(
                        script: "ls -1 LivOnFront/mobile/**/build/outputs/apk/**/*.apk | tail -n 1",
                        returnStdout: true
                    ).trim()
                    if (!apk) {
                        error "⚠️ APK 파일을 찾지 못했습니다. 빌드 산출물 경로를 확인하세요."
                    }

                    def shortSha = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    def stamp    = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
                    def outName  = "livon-${BRANCH_NAME}-${stamp}-${shortSha}.apk"

                    // Jenkins 컨테이너에 /downloads 마운트 필요!
                    sh """
                        echo "📤 Publishing APK to /downloads..."
                        cp -f "${apk}" "/downloads/${outName}"
                        ln -sfn "/downloads/${outName}" "/downloads/latest.apk"  # 최신 고정 링크
                        ls -lh "/downloads/${outName}"
                    """

                    echo "📎 Download URL : ${BASEURL}/download/${outName}"
                    echo "📎 Latest Link  : ${BASEURL}/download/latest.apk"
                }
            }
        }

        /* =========================
         *  오래된 APK 정리 (최신 5개 유지)
         * ========================= */
        stage('Prune Old APKs (optional)') {
            when {
                anyOf {
                    changeset pattern: 'LivOnFront/mobile/**', comparator: 'ANT'
                }
            }
            steps {
                sh '''
                    echo "🧹 Pruning old APKs (keep 5 latest)..."
                    ls -tp /downloads/*.apk 2>/dev/null | grep -v '/$' | tail -n +6 | xargs -r rm --
                '''
            }
        }
    }
}
