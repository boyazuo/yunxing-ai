#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

ENV_FILE=".env"
ENV_EXAMPLE=".env.docker.example"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "未找到 $ENV_FILE，正在从 $ENV_EXAMPLE 复制..."
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "请编辑 $ENV_FILE，至少填写 AI_CHAT_API_KEY 与 AI_EMBEDDING_API_KEY"
fi

COMPOSE_FILES=(-f docker-compose.yml)
if [[ "${1:-}" == "--prod" ]]; then
  COMPOSE_FILES+=(-f docker-compose.prod.yml)
  shift
fi

docker compose "${COMPOSE_FILES[@]}" build "$@"
docker compose "${COMPOSE_FILES[@]}" up -d "$@"

echo ""
echo "构建完成。服务启动中..."
docker compose "${COMPOSE_FILES[@]}" ps
