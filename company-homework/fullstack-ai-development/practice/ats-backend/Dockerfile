# ─────────────────────────────────────────────────────────────────────
# ATS Backend Dockerfile · 多阶段构建
#  - builder：maven:3-eclipse-temurin-21 编译 jar
#  - runtime：eclipse-temurin:21-jre-alpine 跑 jar
#
# 构建（仓库根执行）：
#   docker build -f ats-backend/Dockerfile -t ats-backend:latest ats-backend
# 运行（推荐用 infra/docker-compose.prod.yml）：
#   docker run --rm -p 8080:8080 --env-file infra/.env.prod ats-backend:latest
# ─────────────────────────────────────────────────────────────────────

# ── builder ──────────────────────────────────────────────────────────
FROM docker.io/library/maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# 先拷贝 pom 触发依赖 cache 层（pom 不变即跳过 dl）
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# 再拷贝源码编译
COPY src ./src
RUN mvn -B -q -DskipTests package \
    && cp target/*.jar /build/app.jar

# ── runtime ──────────────────────────────────────────────────────────
FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app

# 非 root 运行，最小权限原则
RUN addgroup -S ats && adduser -S ats -G ats \
    && mkdir -p /app/uploads /app/jwt \
    && chown -R ats:ats /app
USER ats

COPY --from=builder /build/app.jar app.jar

# 默认值（生产 compose 会通过 env 覆盖）
ENV ATS_JWT_KEY_DIR=/app/jwt \
    UPLOAD_PATH=/app/uploads \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# health-check 直接打 actuator/health？为简化，用 curl 打 /api/v1/health（已存在）
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/api/v1/health > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
