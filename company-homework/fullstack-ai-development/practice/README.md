# ATS · 招聘管理系统

> 3 天 AI 辅助开发的全栈 MVP · 候选人从投递到入职的完整漏斗 · **状态机看板 + 数据看板**为核心亮点。

[![tests](https://img.shields.io/badge/backend--tests-282%20passing-brightgreen)](?) [![lint](https://img.shields.io/badge/frontend-0%20error%20%2F%200%20warning-brightgreen)](?) [![phase](https://img.shields.io/badge/phase-M5%20delivery-blue)](?)

---

## 技术栈

| 层 | 选型 | 备注 |
|---|---|---|
| 前端 | Vue 3.5 · Vite 6 · TypeScript · Naive UI 2.44 · Pinia · UnoCSS | HTML5 native DnD（看板）· 0 依赖 SVG 漏斗图（数据看板）|
| 后端 | Spring Boot 3.4 · JDK 21 · MyBatis-Plus · JJWT (RS256) · Spring Security | 三层测试分工（纯 JUnit / Mockito / @WebMvcTest），282 case <17s |
| 数据 | PostgreSQL 16 · Redis 7 | tsvector + pg_trgm 全文搜索 · jsonb 看板时间线 |
| 部署 | Docker Compose · Podman compose · 跨平台一键发布脚本 (ps1 / sh) | dev / prod 双 compose · multi-stage Dockerfile |
| AI | Cursor IDE + Anthropic + 自定义 SKILL.md | 全程沉淀活文档 + 通用规约 |

## 核心特性

- **8 态状态机**：`APPLIED → SCREENING_PASS → 三轮面试 → OFFER → HIRED` + REJECTED 终态保护，前后端镜像状态机让拖拽 0 RT 预校验
- **看板拖拽**：原生 HTML5 dnd · 合法目标列高亮 / 非法变暗 · 乐观更新 + 失败回滚 · 浮动"拒绝投放区"小屏可达 · 0 第三方拖拽库
- **数据看板**：本月投递 / Offer / 入职 / 在招岗位 4 指标卡片 + 8 态招聘漏斗（手绘 SVG，0 ECharts 依赖）+ 点击 stage 跳看板对应列
- **简历 PDF 上传**：自定义虚线 dropzone · UUID v4 文件名 + 路径穿越防御 · 双层 size 校验（servlet hard + service soft）
- **面试评价 24h 编辑窗**：作者 24h 内可改 · ADMIN 不限时间 · 其他 HR 即使是 owner 也不能改
- **JWT RS256 + RBAC**：access 短寿命（15min）· refresh 长寿命（30day）走 HttpOnly cookie · ADMIN / HR / CANDIDATE 三角色

## 仓库布局

```
demo-homework/
├── docs/                              # 全部设计文档（活文档 = HTML + SQL）
│   ├── project-selection-analysis.html  Phase 0 选题
│   ├── project-requirements.html        Phase 1 需求
│   ├── project-schema.sql               Phase 2 数据库 DDL
│   ├── project-tech-design.html         Phase 2 技术设计
│   ├── project-dev-plan.html            Phase 3 编码计划
│   └── project-milestones.html          ★ 活文档·进度·决策·风险·给未来 agent 看
├── infra/
│   ├── docker-compose.dev.yml         # 开发：pg + redis
│   ├── docker-compose.prod.yml        # 生产：pg + redis + backend + frontend(nginx)
│   ├── .env.example  /  .env.prod.example
│   ├── jwt/                           # JWT keypair（dev-*.pem 在仓库；prod-*.pem 自行生成）
│   └── scripts/release.{ps1,sh}       # 一键发布
├── ats-backend/
│   ├── Dockerfile                     # multi-stage maven → jre-alpine
│   └── src/main/java/com/ats/
│       ├── auth/                      # M1 认证
│       ├── job/                       # M2 岗位 CRUD + 状态机
│       ├── application/               # M3 投递 + 8 态状态机
│       ├── interview/                 # M4 面试评价 + 24h 编辑窗
│       ├── file/                      # M4 文件上传抽象（FileStorage）
│       └── stats/                     # M5 数据看板
├── ats-frontend/
│   ├── Dockerfile  /  nginx.conf      # 静态 + /api 反代 + history fallback
│   └── src/views/
│       ├── jobs.vue                   # 岗位市场（CANDIDATE）
│       ├── hr/jobs.vue                # 岗位管理（HR/ADMIN）
│       ├── hr/board.vue               # 招聘看板（HR/ADMIN，状态机拖拽）
│       ├── hr/dashboard.vue           # 数据看板（HR/ADMIN，漏斗 + 4 指标）
│       └── me/applications.vue        # 我的投递（CANDIDATE）
└── .cursor/skills/ai-fullstack-dev/SKILL.md   # AI 工作流 + 通用规约（活文档）
```

## 快速开始

### 0. 环境自检

```powershell
bun run check-env
```

需要：JDK 21+ · Maven 3.9+ · Node 20+ · Bun 1.3+ · Docker **或** Podman · OpenSSL

### 1. 启动数据库 + 缓存

```powershell
cp infra/.env.example infra/.env       # 首次
bun run dev:up                          # 后台启动 pg + redis
bun run dev:logs                        # 看日志
bun run dev:reset                       # 清库重来
```

> Compose 文件用通用语法，`docker-compose` v1 / `docker compose` v2 / `podman compose` 都跑。

### 2. 启动后端

```powershell
pwsh -File scripts/new-jwt-keys.ps1     # 首次：生成 RS256 dev 密钥到 infra/jwt
cp ats-backend/.env.example ats-backend/.env
bun run be:dev                          # mvn spring-boot:run · :8080/api/v1
```

### 3. 启动前端

```powershell
cp ats-frontend/.env.example ats-frontend/.env.local
bun run fe:dev                          # http://localhost:5173
```

### 4. 一键发布到生产

```powershell
# 1) 准备 prod env + keypair
cp infra/.env.prod.example infra/.env.prod
# 编辑 infra/.env.prod 替换所有 *** 占位（强密码！至少 32 字符）

# 在 infra/jwt 生成 prod keypair（用 OpenSSL）
cd infra/jwt
openssl genrsa -out prod-private.pem 2048
openssl rsa  -in prod-private.pem -pubout -out prod-public.pem
cd ../..

# 2) 一键发布
bun run prod:release             # Windows · pwsh
bun run prod:release:bash        # Linux / macOS

# 3) 日常运维
bun run prod:up                  # 启动
bun run prod:logs                # 跟日志
bun run prod:down                # 停止（保留卷）
bun run prod:reset               # 销毁卷（DB 全清，慎用）
```

发布脚本会：检查 env / JWT prod keypair → 跑后端测试 → 构建 image → up -d → 60s 健康轮询。

## 测试

```powershell
bun run be:test            # 后端 282 case · <17s
bun run fe:check           # 前端 typecheck + lint · 0 error / 0 warning
bun run be:verify          # 后端 mvn verify（含 integration test）
```

后端测试分三层（详见 `.cursor/skills/ai-fullstack-dev/SKILL.md` §2.4）：

- **L1 纯 JUnit + @TempDir**：算法 / 工具类（如 `JwtServiceTest` 现场生成 keypair · `JobStatusMachineTest` 35 case @ParameterizedTest）
- **L2 Mockito 单测**：Service 业务分支全覆盖（`JobServiceTest` 17 · `ApplicationServiceTest` 20 · `InterviewServiceTest` 15）
- **L3 @WebMvcTest + @MockitoBean**：真实 SecurityFilterChain 但 mock service / mapper，专测 401 / 403 / @PreAuthorize / 错误码 → HTTP 映射

> **不引入 H2 / Testcontainers**：H2 不支持 PG enum + RETURNING + gen_random_uuid()，Testcontainers 拖慢 CI；SQL 正确性靠 dev compose 联调。

## API 概览

```
# 公开
GET    /api/v1/health                       # 健康检查
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh                 # HttpOnly cookie 自动带
POST   /api/v1/auth/logout
GET    /api/v1/auth/me                      # 当前用户

# 岗位（M2）
GET    /api/v1/jobs                         # 7 维过滤 + tsvector 全文搜索
GET    /api/v1/jobs/:id
GET    /api/v1/tags                         # 24 个 seed tag · 4 category
POST   /api/v1/jobs                         # HR + ADMIN
PUT    /api/v1/jobs/:id
PATCH  /api/v1/jobs/:id/status              # 5 态状态机推进
DELETE /api/v1/jobs/:id                     # ADMIN 软删

# 投递（M3）
POST   /api/v1/applications                 # CANDIDATE
GET    /api/v1/applications/me              # 候选人「我的投递」
GET    /api/v1/applications/board           # HR/ADMIN 看板视图
GET    /api/v1/applications/:id             # 详情含 stage_logs 时间线
PATCH  /api/v1/applications/:id/stage       # 状态机推进（HR/ADMIN）

# 面试评价（M4）
GET    /api/v1/applications/:id/interviews
POST   /api/v1/applications/:id/interviews  # HR/ADMIN
PUT    /api/v1/interviews/:id               # 24h 编辑窗（ADMIN 不限）

# 文件（M4）
POST   /api/v1/files/upload                 # multipart/form-data · PDF only
GET    /api/v1/files/**                     # 鉴权下载

# 数据看板（M5）
GET    /api/v1/stats/funnel                 # 8 态投递漏斗（HR/ADMIN）
GET    /api/v1/stats/funnel/export          # 漏斗 CSV 导出（HR/ADMIN）
GET    /api/v1/stats/overview               # 本月概览 4 指标（HR/ADMIN）

# 认证扩展
POST   /api/v1/auth/change-password         # 已登录用户改密

# Admin 用户管理
GET    /api/v1/admin/users                  # 用户列表
PATCH  /api/v1/admin/users/:id              # 更新用户 / 禁用 / 重绑子部门

# Admin 组织树（M6）
GET    /api/v1/admin/departments/tree
POST   /api/v1/admin/departments
PUT    /api/v1/admin/departments/:id
DELETE /api/v1/admin/departments/:id
POST   /api/v1/admin/sub-departments
PUT    /api/v1/admin/sub-departments/:id
DELETE /api/v1/admin/sub-departments/:id

# 文档与健康
GET    /api/v1/swagger-ui.html              # OpenAPI UI（开发）
GET    /api/v1/actuator/health              # Spring Actuator
```

**HR 数据范围**：看板 / 统计 / 岗位协作权限 = 本人创建的岗位 **或** 绑定子部门下的岗位（`hr_sub_departments`）。

完整接口契约见 `docs/project-tech-design.html`。

## 开发进度

按 `docs/project-dev-plan.html` 推进。实时进度 · 决策记录 · 风险台账 · 给未来 agent 的续接说明全部在活文档 `docs/project-milestones.html`：

```powershell
bun run docs:project-milestones    # http://localhost:3004
```

里程碑：

- [x] **M0** 项目基建 · 骨架 + dev compose + /health _(2026-05-22)_
- [x] **M1** 认证模块 · register / login / refresh / logout / me _(2026-05-23)_
- [x] **M2** 岗位 CRUD · 5 态状态机 + 全文搜索 + 7 维过滤 _(2026-05-25)_
- [x] **M3** ⭐ 状态机看板 · 投递 + 看板拖拽 + 审计日志 + 8 态状态机 _(2026-05-25)_
- [x] **M4** 辅助 · 简历 PDF 上传 + 面试评价 + 24h 编辑窗 _(2026-05-26)_
- [x] **M5** 打磨交付 · 数据看板 + 生产 compose + 一键发布 + README _(2026-05-26)_

## 演示账号

```
# Schema 已 seed（首次登录后请改密码）· 三个账号共用同一密码 Admin@123
ADMIN     admin@ats.local        / Admin@123    System Admin
HR        hr@ats.local           / Admin@123    示例 HR · 张萌
CANDIDATE candidate@ats.local    / Admin@123    示例候选人 · 李哲（已投 1 份简历）
```

> 三个账号在 `docs/project-schema.sql` 末尾以同一 BCrypt hash 灌入（密码全部为 `Admin@123`）。
> 候选人也可通过 `/register` 自助注册（默认角色 CANDIDATE）；HR 由 ADMIN 在 `/admin/users` 后台创建。

种子还包含 25 个示例岗位（5 部门 × 5 状态：18 PUBLISHED · 3 DRAFT · 2 PAUSED · 1 CLOSED · 1 ARCHIVED）和 1 条投递（候选人 → "高级 Java 后端工程师"）。改 schema 后需要重置 dev pg 才能让新 seed 生效：

```bash
bun run dev:reset && bun run dev:up        # 删 volume → 重灌 schema
powershell -NoProfile -File scripts/verify-seed.ps1   # 一键验证 3 账号 login + /stats/public
```

> 生产部署前请<strong>为每个账号独立</strong>重新生成 BCrypt hash：
> `bun -e "console.log(await Bun.password.hash('YOUR_PWD', { algorithm: 'bcrypt', cost: 12 }))"`

## 设计参考

UI 风格："克制 + 张扬混搭" — Linear / Vercel Dashboard 的克制留白做基线，Stripe / Resend 的 aurora gradient 做点缀。Token 系统单一真值 (`tokens.css`)，Naive UI / 原生 CSS / UnoCSS 三方共享一套色值。详见 `.cursor/skills/ai-fullstack-dev/SKILL.md` §2.5。

## License

Personal / homework project.
