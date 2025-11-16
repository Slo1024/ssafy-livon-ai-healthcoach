# ---------- builder ----------
FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /workspace

# (옵션) 캐시 최적화: 의존성 먼저 받기
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle --version

# 실제 소스 복사 후 빌드
COPY . .
RUN gradle clean bootJar --no-daemon

# --------- 실행용 이미지 구성 ---------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안상 root 대신 비루트 사용자(app) 생성. -S는 시스템 계정/그룹(가벼움)
RUN addgroup -S app && adduser -S app -G app
RUN mkdir -p /app/config && chown -R app:app /app

# 빌드 산출물 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
# application.yml 복사 (JAR 안에서 참조될 위치로) / 🔑 yml 소유자를 app으로 지정
COPY --chown=app:app application.yml /app/config/application.yml

USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=file:/app/config/application.yml"]
