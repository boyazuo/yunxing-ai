#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_FILES=(-f docker-compose.yml)
if [[ "${1:-}" == "--prod" ]]; then
  COMPOSE_FILES+=(-f docker-compose.prod.yml)
  shift
fi

docker compose "${COMPOSE_FILES[@]}" down "$@"
echo "云行 AI 已停止"
