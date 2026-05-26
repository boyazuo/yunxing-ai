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

docker compose up -d "$@"

echo ""
echo "云行 AI 已启动"
echo "  前端: http://localhost:3000"
echo "  后端: http://localhost:8080"
echo "  Swagger: http://localhost:8080/swagger-ui.html"
echo "  生产部署请配置宿主机 Nginx: docker/nginx/yunxing-ai.conf"

docker compose ps
