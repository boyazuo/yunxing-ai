#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

ENV_FILE=".env"
if [[ ! -f "$ENV_FILE" ]]; then
  cp .env.docker.example "$ENV_FILE"
  echo "已创建 $ENV_FILE，请填写必要配置后重新运行"
  exit 1
fi

COMPOSE_FILES=(-f docker-compose.yml)
if [[ "${1:-}" == "--prod" ]]; then
  COMPOSE_FILES+=(-f docker-compose.prod.yml)
  shift
fi

docker compose "${COMPOSE_FILES[@]}" up -d "$@"

echo ""
echo "云行 AI 已启动"
if [[ " ${COMPOSE_FILES[*]} " == *" docker-compose.prod.yml "* ]]; then
  echo "  生产模式：api/web 仅监听本机"
  echo "  请确保宿主机 Nginx 已配置并 reload"
  echo "  配置参考: docker/nginx/yunxing-ai.conf"
else
  echo "  前端: http://localhost:3000"
  echo "  后端: http://localhost:8080"
  echo "  Swagger: http://localhost:8080/swagger-ui.html"
fi

docker compose "${COMPOSE_FILES[@]}" ps
