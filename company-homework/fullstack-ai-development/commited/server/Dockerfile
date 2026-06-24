# 多阶段构建：编译 api/migrate/seed 三个二进制，运行期启动前先迁移+种子。
FROM golang:1.26 AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /out/api ./cmd/api \
 && CGO_ENABLED=0 go build -o /out/migrate ./cmd/migrate \
 && CGO_ENABLED=0 go build -o /out/seed ./cmd/seed

FROM alpine:3.20
WORKDIR /app
# 静态二进制（CGO_ENABLED=0）连接 PG/Redis 无需 TLS，故不装 ca-certificates。
COPY --from=build /out/ /app/
COPY migrations /app/migrations
EXPOSE 8080
# 迁移幂等、种子在已存在数据时自动跳过，可安全重启。
CMD ["sh", "-c", "/app/migrate && /app/seed && /app/api"]
