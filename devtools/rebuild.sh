#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

echo "==> Stopping and removing containers"
docker compose down

echo "==> Rebuilding images (no cache, pulling fresh base)"
docker compose build --no-cache --pull

echo "==> Starting containers"
docker compose up -d

echo "==> Pruning builder cache"
docker builder prune -a -f

echo "==> Pruning unreferenced images"
docker image prune -a -f

echo "==> Disk usage after rebuild"
docker system df
