# 07 · 工程规范

> 版本 v0.2 ｜ 创建 2026-06-10 ｜ 最后编辑 2026-06-10 ｜ 状态 草稿 ｜ 作者 XXX

工程规范是考核的一部分。本文约定**编码、协作、提交、完成标准**，全程遵循。

---

## 1. 仓库与目录

- Monorepo：`server/`（Go）、`web/`（Nuxt）、`docs/`（文档）。
- 根目录提供 `docker-compose.yml`、`README.md`、`.env.example`。
- **任务与工具链统一用 mise**（本项目以 mise 作 task runner + devtools 版本管理）：
  - 工具版本锁定在 `mise.toml`（根）与 `server/mise.toml`（go 1.26）等子项目配置；新增工具走 mise，不手动装全局。
  - 任务用 `mise run <task>`：根任务 `up/down/deps/logs`（docker 编排），后端任务 `//server:build|test|fmt|migrate|seed|api|dev`。**不再用 Makefile**。
  - 新建子项目（如前端 `web/`）需在根 `mise.toml` 的 `[monorepo].config_roots` 注册，并 `mise trust` 后任务方可被发现。
- 目录约定见 [02-architecture.md](02-architecture.md) §2/§3，不随意新建顶层目录。

---

## 2. 编码规范

### 2.1 后端 Go
- 遵循 `gofmt`/`goimports`，`golangci-lint` 零告警；包名小写、无下划线。
- 分层纪律：`handler` 只做出入参与校验；`service` 放业务与事务；`repo` 只做数据访问。**跨域只走 service 接口，禁止跨域直接读表**。
- 错误处理：错误就地 `wrap`（`fmt.Errorf("...: %w", err)`），在 handler 统一转错误码；不吞错。
- 上下文透传 `context.Context`；DB 写涉及多表用显式事务。
- 不在代码硬编码密钥/连接串，统一走 `config`。

### 2.2 前端 Nuxt/TS
- ESLint（@antfu 规则集或项目既有）+ `vue-tsc`/`nuxi typecheck` 通过；禁用 `any` 兜底（必要时 `unknown` + 收窄）。
- 组件：`<script setup lang="ts">`；基础组件放 `components/ui`，业务组件放 `components/biz`。
- 数据获取统一走 `useApi`/`useFetch` 封装，不在组件里散落 axios。
- 枚举/文案集中在 `utils/enums.ts`，禁止散落硬编码中文状态。
- 状态：全局态进 Pinia，局部态用 `ref/reactive`；避免 prop 透传地狱用 `provide/inject` 或 store。

### 2.3 命名
- 标识符英文 `snake_case`（DB）/`camelCase`（TS 变量）/`PascalCase`（组件、Go 导出类型）。
- DB 表复数、字段 `snake_case`；接口路径复数名词；枚举值小写下划线。

---

## 3. Git 规范

### 3.1 分支
- `main`：可交付稳定分支。
- 功能分支：`feat/<scope>`、`fix/<scope>`、`docs/<scope>`；小步提交，频繁合并。

### 3.2 提交信息（Conventional Commits）
```
<type>(<scope>): <subject>

[可选 body：为什么这么改]
```
- `type`：`feat | fix | docs | refactor | test | build | chore | style | perf`。
- `scope`：模块名（`org/employee/process/approval/web/server/docs/deploy`…）。
- 示例：`feat(process): 调动流程生效后写入任职履历与人事动态`。
- 一次提交聚焦一件事；提交信息说清「做了什么 / 为什么」。

### 3.3 提交节奏
- 按 `06-roadmap.md` 的 WBS 勾选项粒度提交；每完成一个可验证的小功能即提交。
- 文档与代码同源演进：接口/数据结构变更，连带更新 `docs/03`、`docs/04` 并 bump 版本。

---

## 4. 文档规范（过程性文档）

- 全部过程文档置于 `docs/`，编号递进；`requirements.md` **只读不可编辑**。
- 每篇头部带元信息行（版本/创建/最后编辑/状态/作者），底部带「变更记录」表；实质修改即 bump 版本并追加记录。
- 单一事实源：数据→`03`、接口→`04`、产品→`01`；交叉引用而非复制。
- 需要演示/汇报的内容用 `docs/slides/`（Slidev）承载。

---

## 5. 配置与密钥

- `.env` 不入库，提供 `.env.example` 占位；密钥仅在部署环境注入。
- 默认演示密码 `ChangeMe@123` 仅用于内网 demo，README 注明。
- 日志不打印明文 PII/密钥；`audit_logs.body` 做脱敏与长度截断。

---

## 6. Definition of Done（完成标准）

一个功能项「完成」需同时满足：

- [ ] 功能按 `01` 的产品定义可用，三态（加载/空/错）齐全。
- [ ] 接口符合 `04` 约定（响应/分页/错误码/权限点）。
- [ ] 数据落点符合 `03`，写操作有审计 + 关键变更有 `hr_events`。
- [ ] 权限正确（功能权限 + 数据范围 + 字段脱敏）。
- [ ] 通过 Lint/类型检查；关键逻辑有最小测试或自测脚本。
- [ ] 已提交（规范 commit），相关文档已同步。

---

## 7. 评审 Checklist（自查）

- 安全：鉴权/越权/SQL 注入（参数化）/敏感信息泄露。
- 一致性：枚举文案、日期数字格式、错误码统一。
- 健壮性：空值/边界/并发（流程重复提交幂等）。
- 性能：列表分页与索引、N+1 查询、导出限量。
- 体验：危险操作二次确认、可达性、文案准确。

---

## 变更记录

| 版本 | 日期 | 作者 | 说明 |
| --- | --- | --- | --- |
| v0.1 | 2026-06-10 | XXX | 初稿：目录/编码/Git/文档/配置规范、DoD、评审 checklist |
| v0.2 | 2026-06-10 | XXX | 新增 mise 工具链/任务规约，移除 Makefile 约定 |
