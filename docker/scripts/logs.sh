#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_FILES=(-f docker-compose.yml)
SERVICE="${1:-}"

if [[ "${1:-}" == "--prod" ]]; then
  COMPOSE_FILES+=(-f docker-compose.prod.yml)
  SERVICE="${2:-}"
fi

if [[ -n "$SERVICE" ]]; then
  docker compose "${COMPOSE_FILES[@]}" logs -f "$SERVICE"
else
  docker compose "${COMPOSE_FILES[@]}" logs -f
fi
