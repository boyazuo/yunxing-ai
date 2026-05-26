#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "警告：此操作将删除所有容器与数据卷（Qdrant、上传文件）"
read -r -p "确认继续？[y/N] " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
  echo "已取消"
  exit 0
fi

docker compose down -v --remove-orphans
echo "已清理所有容器与数据卷"
