# 02 · 技术架构

> 版本 v0.3 ｜ 创建 2026-06-10 ｜ 最后编辑 2026-06-10 ｜ 状态 草稿 ｜ 作者 XXX
>
> 关联：选型摘要见 [00-overview.md](00-overview.md)，数据见 [03-data-model.md](03-data-model.md)，接口见 [04-api-design.md](04-api-design.md)

---

## 0. 仓库总览

本仓库为 **polyglot monorepo**，由 **mise** 统一编排（任务 + 工具版本）：

```
homework/
├── mise.toml            工具版本 + 任务 + monorepo config_roots(server/ web/)
├── docker-compose.yml   db(5433) + redis(6380) + server(8080)
├── hk.pkl               git 钩子（polyglot：server 跑 gofmt/vet，web 委托 mise）
├── docs/                过程性文档（00–07）+ slides/（Slidev）
├── server/              Go 后端（独立 module，自带 mise.toml）
│   ├── cmd/             api / migrate / seed
│   ├── internal/        infra · middleware · common · 业务域
│   └── migrations/      顺序 SQL（schema_migrations 记录）
├── web/                 Nuxt 3 前端（自带 mise.toml / hk.pkl / lint 配置）
└── .agents/skills/      已装技能（huashu-design 等）
```

后端业务域（`internal/<module>`，handler/service/repo/model 同包分层）：
`auth · org · employee · process(入离调转) · approval · timeline · analytics`；
横切：`middleware`(requestid/recover/cors/auth/audit) · `common`(响应/错误码/分页) · `infra`(config/db/logger/idgen)。

> 早期 starter 的 TS 库模板（packages/ playground/ 等）已清理，根目录仅保留 HR 项目所需。

---

## 1. 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                          浏览器 (内网)                          │
└───────────────┬──────────────────────────────────────────────┘
                │ HTTPS
        ┌───────▼────────┐
        │   Nuxt 3 (web)  │  SSR/CSR 混合；静态资源 + 页面 + 可选 BFF
        │  Nitro server   │  /api 代理与少量聚合（BFF），其余直连后端
        └───────┬────────┘
                │ REST (JSON, JWT Bearer)
        ┌───────▼────────┐
        │  Go API (server)│  Gin 路由 → middleware → handler → service → repo
        │   Gin + Bun     │  鉴权/审计/限流/请求ID/CORS/recover
        └───┬────────┬────┘
            │        │
    ┌───────▼──┐  ┌──▼───────┐
    │PostgreSQL│  │  Redis   │   会话/缓存/提醒任务队列(轻量, goroutine+cron)
    │   16     │  │   7      │
    └──────────┘  └──────────┘
```

- **前端 Nuxt 3**：约定式路由 + 布局；Pinia 管全局态；`@nuxt/server`(Nitro) 提供 BFF（统一封装鉴权头、聚合若干列表请求、规避 CORS）。生产可纯前端直连后端，BFF 仅做可选优化。
- **后端 Go**：单体进程，按业务域分包（`internal/<module>`）。同步审批 + 轻量后台任务（cron 扫描合同到期/生日，不引入 Asynq，先用 goroutine + ticker，预留接口可换）。
- **PostgreSQL**：主存储，树用 materialized path，半结构化用 JSONB。
- **Redis**：JWT 黑名单/会话、热点缓存、提醒去重；可选。

---

## 2. 后端分层与目录（沿用参考工程约定）

```
server/
├── cmd/
│   ├── api/main.go          HTTP 入口（装配路由、middleware、优雅退出）
│   ├── migrate/main.go      顺序执行 migrations/*.sql
│   └── seed/main.go         写入种子数据（组织/角色/演示账号/字典/造数）
├── internal/
│   ├── infra/               基础设施（不含业务）
│   │   ├── config/          env 配置（godotenv）
│   │   ├── db/              Bun + pgdriver 连接池
│   │   ├── cache/           Redis 客户端
│   │   ├── logger/          zerolog
│   │   ├── idgen/           Snowflake int64 ID
│   │   └── job/             cron/ticker 轻量任务调度
│   ├── middleware/          recover / cors / requestid / auth(JWT→Principal) / scope(数据权限) / audit
│   ├── common/              统一响应、错误码、分页、校验、导出(excelize)
│   ├── org/                 组织/部门/岗位/职级
│   ├── employee/            花名册（含导入导出、字段配置、列偏好）
│   ├── archive/             档案库 + 借阅
│   ├── process/             入离调转（统一人事流程单）
│   ├── approval/            通用审批引擎（实例/步骤）
│   ├── contract/            合同 + 模板 + 到期提醒
│   ├── performance/         任职资格/晋升/奖惩/考核
│   ├── care/                员工关怀（规则 + 记录 + 统计）
│   ├── analytics/           统计分析（聚合查询）
│   ├── report/              报表（模板/生成/导出/定时）
│   ├── timeline/            人事动态(hr_events) + 操作日志(audit)读取
│   ├── auth/                登录/JWT/会话/当前用户
│   └── admin/               角色/字典/字段/提醒规则配置
├── migrations/              0001_*.sql ... 顺序迁移（含 seed 注释）
├── schema.sql               schema 导出（DDL 参考，无数据）
├── .env.example
├── Dockerfile / .air.toml   开发热重载
└── go.mod
```

**模块内统一五件套**：`models.go`（Bun model）｜`repo.go`（数据访问）｜`service.go`（业务规则/事务）｜`handler.go`（HTTP 入参出参）｜`router.go`（注册路由 + 权限点）。跨域只能通过 service 接口调用，**不跨域直接读表**。

---

## 3. 前端结构（Nuxt 3）

```
web/
├── nuxt.config.ts
├── app.vue
├── assets/css/             Tailwind 入口 + 设计 token（CSS 变量）
├── layouts/                default(带侧边导航) / blank(登录)
├── pages/                  约定式路由，对应信息架构
│   ├── index.vue           工作台
│   ├── org/ ...            组织架构
│   ├── roster/ ...         花名册（列表 + [id] 档案详情）
│   ├── affairs/ ...        入离调转 + 审批
│   ├── contracts/ ...      合同
│   ├── archives/ ...       档案
│   ├── performance/ ...    任职奖惩
│   ├── care/ ...           关怀
│   ├── analytics/ ...      统计看板
│   ├── reports/ ...        报表
│   ├── timeline/ ...       人事动态/日志
│   ├── settings/ ...       系统设置
│   └── login.vue
├── components/
│   ├── ui/                 基础组件（Button/Card/Modal/Drawer/Table/Pagination/Badge/Avatar/EmptyState/Skeleton/Toast）
│   └── biz/                业务组件（OrgTree/EmployeeTable/ApprovalTimeline/StatCard/Chart*）
├── composables/            useApi/useAuth/usePermission/usePagination/useTable
├── stores/                 auth/org/ui（Pinia）
├── server/api/             Nitro BFF（可选：登录代理、聚合接口）
├── utils/                  格式化、导出、常量、枚举映射
└── middleware/             auth 路由守卫、权限守卫
```

> 与参考工程（Vite-SPA）相比，目录心智一致（ui/composables/stores/api 分层保留），但路由从 `vue-router` 手写改为 Nuxt 约定式，数据获取用 `useFetch/useAsyncData`。

---

## 4. 横切关注点（Cross-cutting）

| 关注点 | 方案 |
| --- | --- |
| **鉴权** | JWT（access + 可选 refresh），登录签发；`middleware/auth` 解析为 `Principal{user_id, org_path, roles, perms}` 注入 ctx |
| **数据权限** | `middleware/scope` + repo 层统一拼 `path LIKE ?` 与「本人」规则；敏感字段按 `pii` 权限脱敏 |
| **审计** | `middleware/audit` 对所有写请求落 `audit_logs`（user/method/path/status/ip/request_id/body 摘要） |
| **业务事件** | service 层在关键变更后写 `hr_events`（人事动态时间线），与审计互补（业务语义 vs 技术留痕） |
| **统一响应** | `common.Resp{code,message,data,request_id}`；分页 `{list,total,page,page_size}`；错误码表见 `04` |
| **请求追踪** | `requestid` 中间件生成/透传 `X-Request-Id`，贯穿日志与审计 |
| **校验** | `go-playground/validator` 绑定校验 + 业务校验在 service |
| **配置** | `.env` + godotenv；区分 dev/prod；密钥不入库 |
| **日志** | zerolog 结构化日志，含 request_id / user_id |
| **导入导出** | `excelize` 生成/解析 Excel；PDF 用模板渲染（报表 P2，可后置） |
| **限流/熔断** | 简单 IP/用户限流中间件（可选） |
| **错误恢复** | `recover` 中间件统一兜底 500 |

---

## 5. 部署架构

```
docker-compose.yml
├── db        postgres:16            数据卷持久化，初始化执行 migrations
├── redis     redis:7                可选
├── server    go build → 单二进制     启动前跑 migrate + seed（首次）
└── web       nuxt build → node/Nitro 或静态 + nginx
        反向代理：nginx/Caddy 暴露 80/443 → web；/api → server
```

- **一键拉起**：`mise run up`（= `docker compose up -d --build`）；`mise run //server:seed` 写造数。任务统一由 **mise** 管理（task runner + devtools 版本，Go 版本锁定于 `server/mise.toml`）。
- **环境**：内网单机；对外暴露一个端口（如 `http://<intranet-ip>:8080`）。
- **健康检查**：`/healthz`（server）、`/`（web）；compose `healthcheck` 串起依赖顺序。
- **配置**：`.env`（DB/Redis/JWT secret/端口）；提供 `.env.example`。

---

## 6. 关键技术决策（ADR 摘要）

| 决策 | 选择 | 备选 | 理由 |
| --- | --- | --- | --- |
| 树结构存储 | Materialized Path（`path`） | 邻接表 / 闭包表 / ltree | 读多写少、子树查询简单（`LIKE 前缀`），与参考工程一致 |
| 审批引擎 | 通用 `approval_instances/steps` 串行多级 | 每业务自带状态字段 / BPMN 引擎 | 复用度高、够用、工期可控 |
| 后台任务 | goroutine + cron（ticker） | Asynq/消息队列 | 工期紧、量小；预留接口可平滑替换 |
| 附件存储 | 本地磁盘 / DB（base64）占位 | MinIO/OSS | 免外部依赖；抽象 `storage` 接口便于后续替换 |
| 前端渲染 | Nuxt 3 SSR + 客户端水合 | 纯 SPA | 用户指定 Nuxt；首屏与 SEO 友好，BFF 便于聚合 |
| ID 生成 | Snowflake BIGINT | UUID / 自增 | 有序、不暴露规模、分布式友好 |

---

## 变更记录

| 版本 | 日期 | 作者 | 说明 |
| --- | --- | --- | --- |
| v0.1 | 2026-06-10 | XXX | 初稿：系统/后端/前端结构、横切关注点、部署、ADR |
| v0.2 | 2026-06-10 | XXX | 部署/启动改用 mise 任务（mise 作 task runner + 工具版本管理） |
| v0.3 | 2026-06-10 | XXX | 新增 §0 仓库总览（整体目录树），反映实际后端业务域 |
