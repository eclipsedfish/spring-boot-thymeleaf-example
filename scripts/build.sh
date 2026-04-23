#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./mvnw --batch-mode --no-transfer-progress clean package

echo "Build complete. Artifact: target/spring-boot-thymeleaf-example-0.0.1-SNAPSHOT.jar"
