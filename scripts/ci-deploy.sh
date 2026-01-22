#!/bin/bash

set -e

echo "🚀 [CI Script] 작업을 시작합니다..."

if [ -z "$ECR_REGISTRY" ] || [ -z "$ECR_REPOSITORY" ] || [ -z "$IMAGE_TAG" ]; then
  echo "🚨 [Error] 필수 환경변수(ECR 정보)가 누락되었습니다."
  exit 1
fi

echo "🔍 [Info] ECR Info: $ECR_REGISTRY / $ECR_REPOSITORY : $IMAGE_TAG"

# 권한 부여
chmod +x gradlew


echo "☕ [Build] Gradle 빌드 시작..."
./gradlew clean build -x test

# AWS ECR 로그인
echo "🔑 [AWS] ECR 로그인 시도..."
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$ECR_REGISTRY"

# 도커 이미지 빌드
echo "🐳 [Docker] 이미지 빌드..."
docker build -t "$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" .

# 도커 이미지 푸시
echo "🚀 [Docker] ECR로 푸시..."
docker push "$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG"

echo "✅ [Success] 모든 작업이 완료되었습니다."