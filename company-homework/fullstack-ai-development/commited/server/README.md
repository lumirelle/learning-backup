# OrgHR · 后端（server）

组织人事管理系统后端：Go 1.26 + Gin + Bun ORM + PostgreSQL 16。
设计见 [../docs/02-architecture.md](../docs/02-architecture.md)、数据见 [../docs/03-data-model.md](../docs/03-data-model.md)、接口见 [../docs/04-api-design.md](../docs/04-api-design.md)。

## 目录

```
cmd/        api / migrate / seed 三个入口
internal/
  infra/    config · db(Bun) · logger(zerolog) · idgen(snowflake)
  middleware/ requestid · recover · cors · auth(JWT) · audit
  common/   统一响应 · 错误码 · 分页
  auth/ org/ employee/   业务域（handler/service/repo/model 同文件分层）
  api/      路由装配
migrations/ 顺序 SQL 迁移（schema_migrations 记录已执行）
```

> 任务统一用 **mise** 运行（本项目以 mise 作 task runner + devtools 版本管理）。
> 可从仓库根用 `mise run //server:<task>`，或进入 `server/` 后用 `mise run <task>`。

## 本地运行

```bash
# 1) 起依赖（Postgres:5433, Redis:6380）—— 根任务
mise run infra          # = docker compose up -d db redis

# 2) 配置环境
cp server/.env.example server/.env

# 3) 迁移 + 种子 + 启动（也可分开 //server:migrate / :seed / :api）
mise run //server:dev
```

> 注意：本机 8080 端口可能被占用，可用 `APP_PORT=8090 mise run //server:api` 改端口。
> API 默认 `http://localhost:8080`，健康检查 `GET /healthz`。Go 版本由 mise 锁定（`server/mise.toml` → go 1.26）。

## 一键容器化（含前端见根 compose）

```bash
mise run up            # 构建并启动 db + redis + server，自动迁移+种子
mise run logs
```

## 演示账号（密码均为 `ChangeMe@123`）

| 账号 | 角色 | 说明 |
| --- | --- | --- |
| super_admin | 超级管理员 | 全量数据 |
| hr01 | 人事管理员 | 人事操作 |
| mgr01 | 部门经理 | 本部门 + 审批 |
| emp01 | 普通员工 | 仅本人 |

## 快速自测

```bash
# 登录拿 token
curl -s localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"super_admin","password":"ChangeMe@123"}'

# 用 token 访问
TOKEN=...
curl -s localhost:8080/api/v1/orgs/tree -H "Authorization: Bearer $TOKEN"
curl -s 'localhost:8080/api/v1/employees?page=1&page_size=10' -H "Authorization: Bearer $TOKEN"
```

## 已实现（M1 + 核心切片）

- 鉴权：登录 / 当前用户 / JWT 中间件
- 组织架构：组织树、部门树、岗位、职级（读 + 组织/部门新增带 path 计算）
- 员工花名册：分页检索 + 详情 + 动态时间线（数据权限按 org_path 子树）
- 横切：请求 ID、recover、CORS、操作日志审计

> 后续按 [../docs/06-roadmap.md](../docs/06-roadmap.md) 扩展入离调转审批闭环、统计看板与其余功能域。
