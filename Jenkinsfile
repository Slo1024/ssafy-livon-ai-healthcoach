pipeline {
    agent any

    environment {
        BRANCH_NAME = "${env.GIT_BRANCH}".replaceAll(".*/", "")
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'rm -f .apk_built .apk_latest_url || true'
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

                    withCredentials([
                        file(credentialsId: PROPERTIES_ID, variable: 'APP_PROPS_FILE'), 
                        file(credentialsId: 'gcp-key', variable: 'GCP_KEY_FILE')
                    ]) {
                        dir('LivOnBack') {
                            sh '''
                                echo "📦 Copying application.yml..."
                                rm -f application.yml
                                cp -f "$APP_PROPS_FILE" application.yml
                            '''
                        }

                        sh """
                            echo "🔐 워크스페이스 루트에 keys 폴더 생성 및 GCP 키 복사..."
                            rm -rf keys
                            mkdir -p keys
                            cp -f "$GCP_KEY_FILE" keys/livon-477113-1cbd80f7207d.json
                            
                            echo "📂 현재 위치: $(pwd)"
                            echo "📄 keys 폴더 내용:"
                            ls -l keys
                        """

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
         *  APK 공개(/download)
         * ========================= */
        stage('Setup Android SDK') {
            when {
                anyOf { changeset pattern: 'LivOnFront/mobile/**', comparator: 'ANT' }
            }
            steps {
                script {
                    echo '🛠️ Setting up Android SDK...'
                    withEnv([
                        'ANDROID_SDK_ROOT=/var/jenkins_home/android-sdk',
                        'ANDROID_HOME=/var/jenkins_home/android-sdk',
                        'PATH=/var/jenkins_home/android-sdk/cmdline-tools/latest/bin:/var/jenkins_home/android-sdk/platform-tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
                    ]) {
                        // 1) commandline-tools 설치(없으면)
                        sh '''
                            set -e
                            SDK="$ANDROID_SDK_ROOT"
                            mkdir -p "$SDK"
                            if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
                                echo "[+] Installing Android commandline-tools..."
                                cd /tmp
                                # 구글 공식 cmdline-tools 최신 버전 다운로드 (버전은 수시로 바뀜; 'latest' 링크 사용)
                                curl -fsSL -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip || \
                                curl -fsSL -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
                                rm -rf cmdline-tools && mkdir -p cmdline-tools
                                unzip -q cmdline-tools.zip -d cmdline-tools
                                mkdir -p "$SDK/cmdline-tools/latest"
                                # 압축 해제 디렉토리 구조에 따라 'bin' 포함 폴더를 latest 밑으로 이동
                                mv cmdline-tools/cmdline-tools/* "$SDK/cmdline-tools/latest/" 2>/dev/null || \
                                mv cmdline-tools/* "$SDK/cmdline-tools/latest/"
                                rm -rf cmdline-tools cmdline-tools.zip
                            fi
                        '''

                        // 2) 필요한 플랫폼/빌드툴 설치
                        //   compileSdkVersion에 맞춰 골라줘야 하지만, 보통 34~35를 쓰니 둘 다 깔아 안전하게 처리
                        sh '''
                            set -e
                            yes | sdkmanager --licenses >/dev/null
                            sdkmanager --install "platform-tools" || true
                            sdkmanager --install "platforms;android-35" "build-tools;35.0.0" || true
                            sdkmanager --install "platforms;android-34" "build-tools;34.0.0" || true
                        '''

                        // 3) local.properties 생성 (Gradle이 SDK 경로 인식)
                        withCredentials([file(credentialsId: 'mobile-local-properties', variable: 'LOCAL_PROPS_FILE')]) {
                            dir('LivOnFront/mobile') {
                                sh '''
                                    set -e
                                    cp "$LOCAL_PROPS_FILE" local.properties
                                    grep -q '^sdk.dir=' local.properties && sed -i 's|^sdk\\.dir=.*|sdk.dir='"$ANDROID_SDK_ROOT"'|' local.properties || echo "sdk.dir=$ANDROID_SDK_ROOT" >> local.properties
                                    echo "[ok] Generated local.properties:"
                                    cat local.properties
                                '''
                            }
                        }
                    }
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

                    withEnv([
                        'ANDROID_SDK_ROOT=/var/jenkins_home/android-sdk',
                        'ANDROID_HOME=/var/jenkins_home/android-sdk',
                        'PATH=/var/jenkins_home/android-sdk/cmdline-tools/latest/bin:/var/jenkins_home/android-sdk/platform-tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
                    ]) {
                        dir('LivOnFront/mobile') {
                            sh '''
                                chmod +x ./gradlew || true
                            '''
                            sh "./gradlew clean ${TASK}"
                        }
                    }

                    // Jenkins 내 아티팩트 보관(선택)
                    archiveArtifacts artifacts: 'LivOnFront/mobile/**/build/outputs/apk/**/*.apk', fingerprint: true
                }
            }
        }

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
                        ln -sfn "/downloads/${outName}" "/downloads/livon-latest.apk"  # 최신 고정 링크
                        ls -lh "/downloads/${outName}"
                    """

                    echo "📎 Download URL : ${BASEURL}/download/${outName}"
                    echo "📎 Latest Link  : ${BASEURL}/download/livon-latest.apk"

                    // post 단계에서 APK 알림을 보내기 위한 플래그 및 URL 기록
                    writeFile file: '.apk_built', text: '1'
                    writeFile file: '.apk_latest_url', text: "${BASEURL}/download/${outName}\n"
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
    post {
        success {
            script {
                def branch    = env.BRANCH_NAME ?: "${env.GIT_BRANCH}".replaceAll(".*/", "")
                def shortSha  = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                def epochTs   = sh(script: "date +%s", returnStdout: true).trim()
                def isProd    = (branch == 'master')

                def beContainer    = isProd ? 'livon-be-prod'    : 'livon-be-dev'
                def feContainer    = isProd ? 'livon-fe-prod'    : 'livon-fe-dev'
                def nginxContainer = isProd ? 'nginx-prod'       : 'nginx-dev'

                def headerText = isProd ? '### :crown: Production Deployed! :crown:' : '### :pepe_jam: Dev Updated! :pepe_jam:'
                def baseUrl   = isProd ? 'https://k13s406.p.ssafy.io' : 'https://k13s406.p.ssafy.io:8443'

                // APK 최신 링크가 있으면 알림에 포함
                def apkLatestUrl = null
                def hasLatest = sh(script: '[ -f /downloads/livon-latest.apk ] && echo yes || echo no', returnStdout: true).trim() == 'yes'
                if (hasLatest) {
                    apkLatestUrl = "${baseUrl}/download/livon-latest.apk"
                }

                def attachment = [
                    fallback : "Build 성공 - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    color    : "#2ECC71",
                    pretext  : headerText,
                    title    : "${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    title_link: "${env.BUILD_URL}",
                    fields   : [
                        [title: 'Job',          value: env.JOB_NAME,             short: true],
                        [title: 'Build',        value: "#${env.BUILD_NUMBER}",   short: true],
                        [title: 'Branch',       value: branch,                    short: true],
                        [title: 'Commit',       value: shortSha,                  short: true],
                        [title: 'Docker BE',    value: beContainer,               short: true],
                        [title: 'Docker FE',    value: feContainer,               short: true],
                        [title: 'Docker Nginx', value: nginxContainer,            short: true]
                    ],
                    footer   : 'Jenkins',
                    ts       : (epochTs as Long)
                ]

                if (apkLatestUrl) {
                    attachment.fields << [title: 'Latest APK', value: apkLatestUrl, short: false]
                }

                def attachments = [attachment]

                // APK가 이번 빌드에서 업데이트되었다면, 별도의 카드 추가
                if (fileExists('.apk_built')) {
                    def apkUrl = (fileExists('.apk_latest_url') ? readFile('.apk_latest_url').trim() : (apkLatestUrl ?: "${baseUrl}/download/livon-latest.apk"))
                    def apkAttachment = [
                        color   : '#A4C639',
                        pretext : '### :android: New APK Build Ready!',
                        fields  : [
                            [title: 'Latest APK', value: apkUrl, short: false],
                            [title: 'Branch',     value: branch, short: true],
                            [title: 'Commit',     value: shortSha, short: true]
                        ],
                        footer  : 'Jenkins',
                        ts      : (epochTs as Long)
                    ]
                    attachments << apkAttachment
                }

                def payloadObj = [
                    text       : '@channel',
                    attachments: attachments
                ]

                def json   = groovy.json.JsonOutput.toJson(payloadObj)
                def pretty = groovy.json.JsonOutput.prettyPrint(json)

                withCredentials([string(credentialsId: 'livon-mattermost-webhook-url', variable: 'MM_WEBHOOK')]) {
                    sh """
                        curl -s -X POST -H 'Content-Type: application/json' \
                            -d '${pretty.replace("'", "'\\''")}' "$MM_WEBHOOK" >/dev/null || true
                    """
                }
            }
        }
        failure {
            script {
                def branch    = env.BRANCH_NAME ?: "${env.GIT_BRANCH}".replaceAll(".*/", "")
                def shortSha  = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                def epochTs   = sh(script: "date +%s", returnStdout: true).trim()
                def isProd    = (branch == 'master')

                def headerText = isProd ? '### :x: Production Deploy Failed!' : '### :x: Build Failed!'

                def attachment = [
                    fallback : "Build 실패 - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    color    : "#E74C3C",
                    pretext  : headerText,
                    title    : "${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    title_link: "${env.BUILD_URL}console",
                    fields   : [
                        [title: 'Job',    value: env.JOB_NAME,           short: true],
                        [title: 'Build',  value: "#${env.BUILD_NUMBER}", short: true],
                        [title: 'Branch', value: branch,                  short: true],
                        [title: 'Commit', value: shortSha,                short: true]
                    ],
                    footer   : 'Jenkins',
                    ts       : (epochTs as Long)
                ]

                def payloadObj = [
                    text       : '@channel',
                    attachments: [attachment]
                ]

                def json   = groovy.json.JsonOutput.toJson(payloadObj)
                def pretty = groovy.json.JsonOutput.prettyPrint(json)

                withCredentials([string(credentialsId: 'livon-mattermost-webhook-url', variable: 'MM_WEBHOOK')]) {
                    sh """
                        curl -s -X POST -H 'Content-Type: application/json' \
                            -d '${pretty.replace("'", "'\\''")}' "$MM_WEBHOOK" >/dev/null || true
                    """
                }
            }
        }
        unstable {
            script {
                def branch    = env.BRANCH_NAME ?: "${env.GIT_BRANCH}".replaceAll(".*/", "")
                def shortSha  = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                def epochTs   = sh(script: "date +%s", returnStdout: true).trim()
                def isProd    = (branch == 'master')

                def headerText = isProd ? '### :warning: Production Deploy Unstable!' : '### :warning: Build Unstable!'

                def attachment = [
                    fallback : "Build 불안정 - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    color    : "#F1C40F",
                    pretext  : headerText,
                    title    : "${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    title_link: "${env.BUILD_URL}console",
                    fields   : [
                        [title: 'Job',    value: env.JOB_NAME,           short: true],
                        [title: 'Build',  value: "#${env.BUILD_NUMBER}", short: true],
                        [title: 'Branch', value: branch,                  short: true],
                        [title: 'Commit', value: shortSha,                short: true]
                    ],
                    footer   : 'Jenkins',
                    ts       : (epochTs as Long)
                ]

                def payloadObj = [
                    text       : '@channel',
                    attachments: [attachment]
                ]

                def json   = groovy.json.JsonOutput.toJson(payloadObj)
                def pretty = groovy.json.JsonOutput.prettyPrint(json)

                withCredentials([string(credentialsId: 'livon-mattermost-webhook-url', variable: 'MM_WEBHOOK')]) {
                    sh """
                        curl -s -X POST -H 'Content-Type: application/json' \
                            -d '${pretty.replace("'", "'\\''")}' "$MM_WEBHOOK" >/dev/null || true
                    """
                }
            }
        }
    }
}
