#!/bin/bash
set -e # 에러 나면 즉시 종료

echo "🚀 [CD Script] 젠킨스 배포를 시작합니다..."

# 1. 필수 변수 검증 (젠킨스가 잘 넘겨줬나?)
# (참고: DB 정보 등은 Jenkins Credentials에서 주입됨)
if [ -z "$DB_PASSWORD" ]; then
  echo "🚨 [Error] DB_PASSWORD 환경변수가 없습니다. Jenkins 설정을 확인하세요."
  exit 1
fi

# 변수 기본값 설정 (혹시 비어있으면 이걸로)
ECR_REGISTRY=${ECR_REGISTRY_URL:}
REPO=${ECR_REPOSITORY:-"server-repo"}
TAG=${IMAGE_TAG:-"latest"}
CONTAINER=${CONTAINER_NAME:-"spring_404_jerry"}

echo "🔍 [Info] Target: $ECR_REGISTRY / $REPO : $TAG"

# 2. AWS ECR 로그인
echo "🔑 [AWS] ECR 로그인..."
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$ECR_REGISTRY"

# 3. 최신 이미지 Pull
echo "📥 [Docker] 이미지 다운로드..."
docker pull "$ECR_REGISTRY/$REPO:$TAG"

# 4. 기존 컨테이너 정리 (중단 -> 삭제)
echo "🛑 [Docker] 기존 컨테이너 중지..."
docker stop "$CONTAINER" || true
docker rm "$CONTAINER" || true

# 5. 새 컨테이너 실행 (가장 중요한 부분!)
echo "🔥 [Docker] 새 컨테이너 실행..."
docker run -d -p 8080:8080 \
  --name "$CONTAINER" \
  -e DB_URL="$DB_URL" \
  -e DB_USER="$DB_USER" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e REDIS_HOST= redis \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TZ=Asia/Seoul \
  "$ECR_REGISTRY/$REPO:$TAG"

# 6. 청소
echo "🧹 [Docker] 미사용 이미지 정리..."
docker image prune -f

echo "✅ [Success] 배포가 성공적으로 완료되었습니다."