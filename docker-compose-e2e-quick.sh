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

echo "### Quick start (без пересборки) ###"
echo "### Browser: $BROWSER ###"

echo "### Stop and remove old containers ###"
docker compose --profile test down --remove-orphans || true
docker rm -f allure allure-ui selenoid selenoid-ui rcc-e2e 2>/dev/null || true

echo "### Start all services (no build) ###"
docker compose --profile test up -d --no-build

echo "### Контейнеры запущены ###"
docker ps -a

echo ""
echo "=============================================="
echo "  Selenoid UI : http://localhost:9099/"
echo "  Allure UI   : http://localhost:5252/"
echo "  Allure report: http://localhost:5050/allure-docker-service/projects/rcc/reports/latest/index.html"
echo "=============================================="
echo "  Логи e2e: docker logs -f rcc-e2e"
echo "=============================================="