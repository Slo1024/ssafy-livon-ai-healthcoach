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

# ---------- runtime ----------
# JDK 대신 JRE - 가벼움
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# non-root 실행 / -S는 시스템 계정/그룹(가벼움).
RUN addgroup -S app && adduser -S app -G app
USER app

# 빌드 산출물 복사
# 빌더 스테이지의 산출물(실행 JAR)만 가져와서 /app/app.jar로 배치.
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# 외부 설정
COPY application.yml /app/config/application.yml

EXPOSE 8081

# 기본 경로는 유지 + 추가 경로만 보강
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.config.additional-location=file:/app/config/"]
