# 组织人事管理系统 · OrgHR

> 全栈考核项目 —— 基于人力资源系统的「**组织人事模块**」从 0 到 1 设计与开发。
> 覆盖 产品设计 + 全栈开发 + 交付上线，强调「**端到端闭环**」：录入 → 流程 → 审批 → 留痕 → 可视化。

技术栈：**Nuxt 4 + UnoCSS + Pinia**（前端）｜ **Go + Gin + Bun ORM**（后端）｜ **PostgreSQL 16 + Redis**｜ **mise** 统一编排。

---

## 1. 功能与现状

围绕组织人事 **10 大功能域全部建成**（后端 + 前端页面），核心闭环端到端可演示：

| # | 功能域 | 状态 |
| --- | --- | --- |
| 1 | 组织架构（主体/部门/岗位/职级） | ✅ 树形维护 + 可视化 |
| 2 | 员工花名册（全字段/多维检索/档案） | ✅ 列表/筛选/分页/档案时间线 |
| 3 | 档案库（分类/条目/借阅审批/归还） | ✅ 借阅复用审批引擎 + 归还状态机 |
| 4 | 入离调转（入职/离职/调动/转正 + 审批） | ✅ **核心闭环**：通用审批引擎 + 事务化生效 |
| 5 | 合同管理（签订/续签/终止/到期提醒） | ✅ |
| 6 | 任职奖惩（奖励/惩罚，进时间线） | ✅ |
| 7 | 员工关怀（生日/入职周年名单） | ✅ |
| 8 | 统计分析（人员结构/趋势/司龄/编制） | ✅ 多维看板 |
| 9 | 人事报表（内置模板 + 导出 Excel） | ✅ excelize 真实 .xlsx |
| 10 | 人事动态（变动时间线 + 操作日志审计） | ✅ 业务留痕 + 技术留痕双线 |

> 增强项（非必须）：组织节点移动、花名册批量导入导出、报表定时推送等。

> 完整规划见 [docs/](docs/)（00 总览 / 01 产品 / 02 架构 / 03 数据 / 04 接口 / 05 设计 / 06 排期 / 07 规范）。

---

## 2. 仓库结构

```
homework/
├── server/         Go 后端（独立 module；cmd/ internal/ migrations/）
├── web/            Nuxt 4 前端（app/ pages·components·composables）
├── docs/           过程性文档（00–07）+ slides/（Slidev 演示与验收脚本）
├── deploy/         nginx 反向代理配置
├── docker-compose.yml   一键编排：db + redis + server + web + nginx
├── mise.toml       工具版本 + 任务（task runner）
```

后端业务域：`auth · org · employee · process(入离调转) · approval · timeline · analytics · contract`。

---

## 3. 快速开始

### 1. 本地开发：开发前置依赖

- [mise](https://mise.jdx.dev)（管理 Go 1.26 / Bun / 工具版本与任务）
- Docker + Docker Compose
- 首次进目录 `mise install` 安装工具链

```bash
mise run infra                          # 起依赖：PostgreSQL(5433) + Redis(6380)
APP_PORT=8090 mise run //server:api     # 后端：(8090) 首启自动迁移 + 写种子数据
mise run //web:dev                      # 前端：(3000) 自动把 /api/v1 反代到 8090
```
打开终端 `Local:` 提示的地址（默认 `http://localhost:3000`，被占则顺延）。

### 2: Docker 一键上线（推荐）

```bash
docker compose up -d --build            # db + redis + server + web + nginx
# 对外访问：http://<本机IP>:5555  （由 nginx 统一入口，/api 反代到后端）
# 自定义端口：WEB_PORT=9999 docker compose up -d --build
```
server 容器启动时自动执行**迁移 + 种子**（幂等，可重启）。

此后访问 `http://<本机IP>:5555`（或自定义端口）即可。

---

## 4. 演示账号

| 账号 | 角色 | 数据范围 |
| --- | --- | --- |
| `super_admin` | 超级管理员 | 全量 |
| `hr01` | 人事管理员 | 全公司 |
| `mgr01` | 部门经理 | 本部门 + 审批 |
| `emp01` | 普通员工 | 仅本人 |

密码统一 **`ChangeMe@123`**（种子写入，仅供内网演示）。

---

## 5. 数据库

- **迁移**：`server/migrations/*.sql` 顺序执行，`schema_migrations` 表记录进度（`mise run //server:migrate`）。
- **种子**：`mise run //server:seed`（已存在则跳过；3 组织 / 6 部门 / 12 岗位 / 30 员工 / 4 账号 / 合同）。
- **Schema 导出**：[server/schema.sql](server/schema.sql)（DDL 参考，无数据）。

---

## 6. 手动测试 / 验收

跟着 Slidev 验收脚本点一遍即可走通端到端闭环：

```bash
cd docs/slides && npx @slidev/cli demo.md     # 点到哪 / 看什么 / 预期什么
```
要点：登录 → 发起调动 → 切 mgr01 审批通过 → 员工/履历/动态/审计/看板**五处一致联动**。

---

## 7. 工程

- 任务统一走 **mise**：根 `up/down/infra/logs`；后端 `//server:build|test|migrate|seed|api`；前端 `//web:dev|build`。
- Git 钩子（hk）：提交时对 `server/`(gofmt/vet) 与 `web/`(eslint) 做校验。
- API 约定、错误码、数据权限（org_path 子树）见 [docs/04-api-design.md](docs/04-api-design.md)。
