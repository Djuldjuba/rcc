#!/bin/bash
source ./docker.properties
export PROFILE=docker
export PREFIX="${IMAGE_PREFIX}"

echo "### Stop and remove old containers ###"
docker compose down

echo "### Java version ###"
java --version

echo "### Build backend images with Jib ###"
./gradlew clean jibDockerBuild -x :rcc-e2e:test -Duser.timezone=UTC || {
  echo "jibDockerBuild failed, trying jibBuildTar fallback..."
  ./gradlew jibBuildTar -x :rcc-e2e:test
  for module in rcc-auth rcc-gateway rcc-artist rcc-painting rcc-museum rcc-geo rcc-userdata; do
    if [ -f "$module/build/jib-image.tar" ]; then
      docker load -i "$module/build/jib-image.tar"
    fi
  done
}

echo "### Build frontend ###"
docker build -t ${PREFIX}/rcc-client:latest ./rcc-client

echo "### Start all services ###"
docker compose up -d

echo "### Running containers ###"
docker ps -a