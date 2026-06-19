#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  ATS Release Script · Linux / macOS
#  在仓库根目录执行：
#    bash infra/scripts/release.sh
#
#  Flags：
#    --skip-build   跳过 docker-compose build（复用已有 image）
#    --skip-tests   跳过后端 mvn test
# ─────────────────────────────────────────────────────────────

set -euo pipefail

SKIP_BUILD=0
SKIP_TESTS=0
COMPOSE_BIN="${COMPOSE_BIN:-docker-compose}"

for arg in "$@"; do
  case "$arg" in
    --skip-build) SKIP_BUILD=1 ;;
    --skip-tests) SKIP_TESTS=1 ;;
    *) echo "Unknown arg: $arg" >&2; exit 1 ;;
  esac
done

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$REPO_ROOT"

echo "━━━ ATS Release ━━━"

# 1. env 校验
ENV_FILE="$REPO_ROOT/infra/.env.prod"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "✗ 找不到 $ENV_FILE"
  echo "  请先 cp infra/.env.prod.example infra/.env.prod 并填入实际值"
  exit 1
fi
echo "✓ env file present: $ENV_FILE"

# 2. JWT prod keypair 校验
JWT_PRIV="$REPO_ROOT/infra/jwt/prod-private.pem"
JWT_PUB="$REPO_ROOT/infra/jwt/prod-public.pem"
if [[ ! -f "$JWT_PRIV" || ! -f "$JWT_PUB" ]]; then
  echo "✗ 缺少 prod-private.pem / prod-public.pem"
  echo "  生成命令（在 infra/jwt 内）："
  echo "    openssl genrsa -out prod-private.pem 2048"
  echo "    openssl rsa -in prod-private.pem -pubout -out prod-public.pem"
  exit 1
fi
echo "✓ JWT prod keypair present"

# 3. 测试
if [[ $SKIP_TESTS -eq 0 ]]; then
  echo "━ Running backend tests..."
  bun run be:test
  echo "✓ Backend tests passed"
fi

COMPOSE_FILE="infra/docker-compose.prod.yml"

# 4. 构建
if [[ $SKIP_BUILD -eq 0 ]]; then
  echo "━ Building images..."
  $COMPOSE_BIN -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build
  echo "✓ Images built"
fi

# 5. 启动
echo "━ Starting stack..."
$COMPOSE_BIN -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

# 6. 健康轮询
WEB_PORT="$(grep -E '^WEB_PORT=' "$ENV_FILE" | head -1 | cut -d= -f2 || echo 80)"
WEB_PORT="${WEB_PORT:-80}"
HEALTH_URL="http://127.0.0.1:${WEB_PORT}/api/v1/health"
echo "━ Waiting for $HEALTH_URL (max 60s)..."

for _ in $(seq 60); do
  if curl -fsS --max-time 2 "$HEALTH_URL" 2>/dev/null | grep -q '"code":0'; then
    echo ""
    echo "━━━ READY ━━━"
    echo "  Web:    http://127.0.0.1:${WEB_PORT}/"
    echo "  API:    http://127.0.0.1:${WEB_PORT}/api/v1/health"
    echo "  Logs:   bun run prod:logs"
    echo "  Stop:   bun run prod:down"
    exit 0
  fi
  sleep 1
done

echo "⚠ 60 秒内未就绪，请检查：bun run prod:logs"
exit 2
