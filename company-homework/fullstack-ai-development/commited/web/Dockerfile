FROM oven/bun:slim AS build-stage

WORKDIR /app

COPY package.json bun.lock ./
RUN --mount=type=cache,id=bun-store,target=/root/.bun/install/cache \
    bun install --frozen-lockfile

COPY . .
RUN bun run build

# SSR / 运行期
FROM oven/bun:slim AS production-stage

WORKDIR /app

COPY --from=build-stage /app/.output ./.output

# Nitro 追踪出的 .output/server/node_modules 在纯 bun 安装布局下不完整
# （vue 与 @vue/* 版本错配，运行期报 "Cannot find package 'vue'"）。
# 按产物自带的 package.json（含全部精确版本）重装运行期依赖，得到自洽的 node_modules。
RUN --mount=type=cache,id=bun-store,target=/root/.bun/install/cache \
    cd .output/server && bun install

EXPOSE 3000

CMD ["bun", "run", ".output/server/index.mjs"]
