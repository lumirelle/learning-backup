#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  ATS 环境自检 · Bash / WSL / macOS / Linux
#  用法：bash scripts/check-env.sh
# ─────────────────────────────────────────────────────────────

set +e

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[0;33m'; NC='\033[0m'
fails=0

check() {
  local name="$1"
  local cmd="$2"
  printf "%-30s" "$name"
  if eval "$cmd" >/dev/null 2>&1; then
    printf "${GREEN}OK${NC}\n"
  else
    printf "${RED}X${NC}\n"
    fails=$((fails+1))
  fi
}

port_free() {
  local p="$1"
  if command -v lsof >/dev/null; then
    ! lsof -i:"$p" >/dev/null 2>&1
  elif command -v ss >/dev/null; then
    ! ss -tln "( sport = :$p )" 2>/dev/null | grep -q ":$p"
  else
    ! (echo > /dev/tcp/127.0.0.1/$p) >/dev/null 2>&1
  fi
}

echo ""
echo "ATS · 环境自检"
echo "----------------------------------------"

check "JDK 21+"        'java -version 2>&1 | grep -E "version \"(2[1-9]|[3-9][0-9])"'
check "Maven 3.9+"     'mvn -v 2>&1 | grep -E "Apache Maven 3\.(9|1[0-9])"'
check "Node 20+"       'node -v | grep -E "^v(2[0-9]|[3-9][0-9])"'
check "Bun 1.3+"       'bun -v | grep -E "^1\.([3-9]|[0-9]{2})"'
check "Git"            'git --version'
check "OpenSSL"        'openssl version'

check "容器 (Docker/Podman)" '(docker --version 2>&1 || podman --version 2>&1) | head -n1'
check "Compose (任一)"  '(docker-compose --version 2>&1 || docker compose version 2>&1 || podman compose version 2>&1) | head -n1'

for p in 5432 6379 8080 5173; do
  check "端口 $p 空闲" "port_free $p"
done

echo ""
if [ "$fails" -gt 0 ]; then
  printf "${YELLOW}[!] %d 项未通过，请参考 SKILL.md > Phase 3 > 常见问题预案${NC}\n" "$fails"
  exit 1
fi
printf "${GREEN}[OK] 全部通过，可进入 M0${NC}\n"
