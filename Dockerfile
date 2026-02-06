# ===============================================================
# Multi-stage Dockerfile for Spring Boot Application (Gradle)
# ===============================================================

# ===============================================================
# Stage 1: Build Stage (빌드 전용 환경)
# ===============================================================
FROM gradle:8.5-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 성능 최적화: Gradle 래퍼 및 의존성 파일 먼저 복사
# 이렇게 하면 소스코드 변경 시에도 의존성 다운로드는 캐시 활용 가능
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Gradle 래퍼 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 사전 다운로드 (Docker 레이어 캐싱 최적화)
# 이 단계가 실패하지 않도록 --continue 옵션 사용
RUN ./gradlew dependencies --no-daemon --continue || true

# 소스 코드 복사 (의존성 캐시 이후에 복사하여 빌드 속도 향상)
COPY src ./src

# 애플리케이션 빌드
# -x test: 테스트 건너뛰기 (CI/CD 파이프라인에서 별도 실행)
# --no-daemon: Gradle 데몬 비활성화 (Docker 환경에서 권장)
# --build-cache: 빌드 캐시 활용
RUN ./gradlew clean build -x test --no-daemon --build-cache

# 빌드 결과 확인 (디버깅용)
RUN ls -la build/libs/

# ===============================================================
# Stage 2: Runtime Stage (실행 전용 환경)
# ===============================================================
FROM eclipse-temurin:17-jre-alpine

# 메타데이터 레이블 추가 (이미지 관리용)
LABEL maintainer="your-email@example.com"
LABEL version="1.0.0"
LABEL description="Spring Boot Application for CI/CD Pipeline"

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 실행을 위한 사용자 생성 (보안 강화)
# root 사용자로 실행하지 않음
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 필수 패키지 설치 및 설정
RUN apk add --no-cache \
    # 타임존 설정용
    tzdata \
    # 헬스체크용 (wget 또는 curl)
    wget \
    # 디버깅용 (선택사항)
    curl \
    # 로그 로테이션용
    logrotate && \
    # 캐시 정리
    rm -rf /var/cache/apk/*

# 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 로그 디렉토리 생성
RUN mkdir -p /var/log/application && \
    chown -R appuser:appgroup /var/log/application

# Stage 1에서 빌드된 JAR 파일만 복사
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# 파일 권한 설정
RUN chmod 755 app.jar

# 애플리케이션 포트 노출 (문서화 목적)
EXPOSE 8080

# JVM 메모리 설정을 위한 환경변수
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 헬스체크 설정 (Docker 자체 헬스체크)
HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=60s \
            --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 비root 사용자로 전환 (보안 강화)
USER appuser

# 애플리케이션 실행 명령어
# ENTRYPOINT + CMD 조합으로 유연성 확보
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production} -jar app.jar"]

# 기본 CMD (ENTRYPOINT와 함께 사용)
CMD []
