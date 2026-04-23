#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

IMAGE_NAME="${IMAGE_NAME:-spring-thymeleaf-app}"
IMAGE_TAG="${IMAGE_TAG:-staging}"
CONTAINER_NAME="${CONTAINER_NAME:-spring-thymeleaf-staging}"
PORT="${PORT:-8080}"
RUN_TESTS="${RUN_TESTS:-true}"

if [[ "$RUN_TESTS" == "true" ]]; then
  ./mvnw --batch-mode --no-transfer-progress clean verify
else
  ./mvnw --batch-mode --no-transfer-progress clean package -DskipTests
fi

docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" .

docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
docker run -d --name "${CONTAINER_NAME}" -p "${PORT}:8080" \
  -e SPRING_PROFILES_ACTIVE=staging \
  "${IMAGE_NAME}:${IMAGE_TAG}"

echo "Deployment complete at http://localhost:${PORT}"
