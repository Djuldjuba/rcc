#!/bin/bash
set -e

source ./docker.properties

export COMPOSE_PROFILES=test
export PROFILE=docker
export PREFIX="${IMAGE_PREFIX}"
export ALLURE_DOCKER_API=http://allure:5050/
export HEAD_COMMIT_MESSAGE="${HEAD_COMMIT_MESSAGE:-local build}"
export BUILD_URL="${BUILD_URL:-local}"
export EXECUTION_TYPE="${EXECUTION_TYPE:-local}"
export ARCH=$(uname -m)
export BROWSER="${1:-chrome}"

echo "### Browser: $BROWSER ###"

echo "### Stop and remove old containers ###"
docker compose --profile test down --remove-orphans --volumes || true
docker rm -f allure allure-ui selenoid selenoid-ui rcc-e2e 2>/dev/null || true

echo "### Java version ###"
java --version

echo "### Build backend images with Jib (via TAR) ###"
./gradlew clean jibBuildTar -x :rcc-e2e:test -Duser.timezone=UTC

echo "### Load backend images from TAR into Docker ###"
for module in rcc-auth rcc-gateway rcc-artist rcc-painting rcc-museum rcc-geo rcc-userdata rcc-log-kafka-service; do
  TAR_PATH="$module/build/jib-image.tar"
  if [ -f "$TAR_PATH" ]; then
    echo "Loading $TAR_PATH ..."
    docker load -i "$TAR_PATH"
  else
    echo "WARNING: $TAR_PATH not found!"
  fi
done

echo "### Build frontend ###"
docker build -t ${PREFIX}/rcc-client:latest ./rcc-client
docker tag ${PREFIX}/rcc-client:latest rcc-client:latest || true

echo "### Build e2e image ###"
docker build -t rcc-e2e:latest -f ./rcc-e2e/Dockerfile .

echo "### Pull Selenoid browser images ###"
docker pull selenoid/vnc_chrome:125.0
docker pull selenoid/vnc_chrome:127.0
docker pull twilio/selenoid:chrome_stable_135
docker pull twilio/selenoid:chrome_stable_140
docker pull selenoid/vnc:firefox_125.0

echo "### Start all services + e2e ###"
docker compose --profile test up -d

echo "### Running containers ###"
docker ps -a

echo ""
echo "=============================================="
echo "  Selenoid UI : http://localhost:9099/"
echo "  Allure UI   : http://localhost:5252/"
echo "  Allure report: http://localhost:5050/allure-docker-service/projects/rcc/reports/latest/index.html"
echo "=============================================="
echo "  Логи e2e: docker logs -f rcc-e2e"
echo "=============================================="