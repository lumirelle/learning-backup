# ─────────────────────────────────────────────────────────────────────
# ATS Frontend Dockerfile · 多阶段构建
#  - builder：oven/bun 跑 vite build
#  - runtime：nginx:alpine serve 静态资源 + 反代 /api/v1 到 backend
#
# 构建（仓库根执行）：
#   docker build -f ats-frontend/Dockerfile -t ats-frontend:latest ats-frontend
# 运行（推荐用 infra/docker-compose.prod.yml）：
#   docker run --rm -p 80:80 ats-frontend:latest
# ─────────────────────────────────────────────────────────────────────

# ── builder ──────────────────────────────────────────────────────────
FROM docker.io/oven/bun:1.3-alpine AS builder
WORKDIR /build

# bun.lock + package.json 先 cache 依赖层
COPY package.json bun.lock* ./
RUN bun install --frozen-lockfile

# 源码编译（vite build → dist/）
COPY . .
ENV VITE_API_BASE_URL=/api/v1
RUN bun run build

# ── runtime ──────────────────────────────────────────────────────────
FROM docker.io/library/nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# 删默认页 + 拷贝 dist
RUN rm -rf ./*
COPY --from=builder /build/dist ./

# 自定义 nginx 配置：history fallback + /api 反代
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ > /dev/null || exit 1

CMD ["nginx", "-g", "daemon off;"]
