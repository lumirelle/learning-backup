# 04 · API 设计

> 版本 v0.2 ｜ 创建 2026-06-10 ｜ 最后编辑 2026-06-10 ｜ 状态 草稿 ｜ 作者 XXX
>
> 数据以 [03-data-model.md](03-data-model.md) 为准；产品口径见 [01-product-design.md](01-product-design.md)。

---

## 1. 通用约定

- **风格**：RESTful，前缀 `/api/v1`，资源用复数名词，动作用 HTTP 方法；非 CRUD 动作用 `:action` 子资源（如 `POST /processes/{id}:approve`）。
- **传输**：JSON（UTF-8）；时间用 RFC3339；金额字符串或数字两端约定一致。
- **鉴权**：`Authorization: Bearer <jwt>`；登录/健康检查除外。
- **请求追踪**：客户端可传 `X-Request-Id`，否则服务端生成并回传。

### 1.1 统一响应

```jsonc
// 成功
{ "code": 0, "message": "ok", "data": { /* ... */ }, "request_id": "..." }
// 分页 data
{ "list": [ /* ... */ ], "total": 128, "page": 1, "page_size": 20 }
// 失败
{ "code": 1004, "message": "员工不存在", "data": null, "request_id": "..." }
```

- `code=0` 成功；非 0 为业务错误码（§3）。HTTP 状态码同时语义化（4xx/5xx）。

### 1.2 列表查询通用参数

`page`(默认1) `page_size`(默认20，max100) `keyword` `sort`(如 `-created_at`) `org_id`/`scope`（数据权限范围）+ 各资源特有筛选。

### 1.3 鉴权与权限

- 每个写接口标注所需**权限点**（`perm`）；中间件校验「角色→权限点」与「数据范围」。
- 敏感字段按 `pii` 权限决定明文/掩码返回。

---

## 2. 分模块接口清单

> 表格列：方法 ｜ 路径（省略 `/api/v1` 前缀）｜ 说明 ｜ 权限点。

### 2.1 认证 auth

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 账号密码登录，返回 JWT + 用户信息 | 公开 |
| POST | `/auth/logout` | 注销（JWT 加黑名单） | 登录 |
| GET | `/auth/me` | 当前用户（含 roles/perms/org_path）✅ | 登录 |
| GET | `/users?keyword=` | 账号列表（审批人选择 / 账号管理，范围收敛）✅ | 登录 |
| GET | `/healthz` | 健康检查 | 公开 |

### 2.2 组织架构 org

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/orgs/tree` | 组织主体树 | org:read |
| POST/PUT/DELETE | `/orgs`,`/orgs/{id}` | 增改删（软删） | org:write |
| GET | `/departments/tree?org_id=` | 部门树 | org:read |
| POST/PUT/DELETE | `/departments`,`/departments/{id}` | 增改删 | org:write |
| POST | `/departments/{id}:move` | 移动节点（更新子树 path） | org:write |
| GET/POST/PUT/DELETE | `/positions` | 岗位维护 | org:read/write |
| GET/POST/PUT/DELETE | `/job-levels` | 职级序列维护 | org:read/write |
| GET | `/orgs/chart?root=&view=` | 架构图数据（org/dept/report 视图） | org:read |

### 2.3 员工花名册 employee

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/employees` | 列表（多维筛选 + 分页 + 排序） | employee:read |
| GET | `/employees/{id}` | 档案详情（含各 Tab 聚合或分接口） | employee:read |
| POST/PUT | `/employees`,`/employees/{id}` | 新增/编辑（一般经流程，直改受限） | employee:write |
| GET | `/employees/{id}/timeline` | 该员工的 hr_events 时间线 | employee:read |
| POST | `/employees:import` | 批量导入（上传 Excel，返回校验报告） | employee:import |
| GET | `/employees:import/template` | 下载导入模板 | employee:import |
| GET | `/employees:export` | 导出当前筛选结果 Excel | employee:export |
| GET/PUT | `/list-prefs/{list_key}` | 列表列偏好读写 | 登录 |
| GET/POST/PUT | `/employee-fields` | 可配置字段维护 | admin:config |

### 2.4 入离调转 process + 审批 approval

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/processes?type=&status=` | 流程单列表 | transfer:read |
| POST | `/processes` | 发起（type=onboard/offboard/transfer/regularize） | transfer:apply |
| GET | `/processes/{id}` | 详情（表单 + 审批轨迹 + 时间线）✅ | transfer:read |
| POST | `/processes/{id}/approve` | 审批通过（当前步）✅ | transfer:approve |
| POST | `/processes/{id}/reject` | 驳回 ✅ | transfer:approve |
| GET | `/approvals/todo` | 我的待办审批 ✅ | 登录 |
| GET | `/approvals/mine` | 我发起的 ✅ | 登录 |

> 实现说明：动作端点采用 gin 友好的**子路径**风格 `/{id}/approve|reject`（而非 `:approve` 冒号式）。`submit/cancel` 暂未实现：当前 `POST /processes` 直接进入 `pending`（发起即提交），后续可补草稿态。发起入参：`{type, employee_id, payload, approver_ids[]}`；id 类字段均为字符串。

> 生效（最后一步通过）由服务端在事务内落地（更新员工 + 履历 + 动态 + 流程状态）。

### 2.5 合同 contract

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/contracts?status=&expiring_in=&employee_id=` | 列表（含到期筛选）✅ | contract:read |
| POST | `/contracts` | 签订 ✅ | contract:write |
| GET | `/contracts/:id` | 详情 ✅ | contract:read |
| POST | `/contracts/:id/renew` | 续签（生成新合同关联旧）✅ | contract:write |
| DELETE | `/contracts/:id` | 终止 ✅ | contract:write |
| GET | `/contracts/reminders?days=` | 到期提醒列表（含剩余天数，工作台用）✅ | contract:read |
| GET/POST | `/contract-templates` | 模板管理 ✅ | contract:write |

> 实现说明：终止采用 **`DELETE /contracts/:id`**（而非 `POST /contracts/:id/terminate`）。原因是 gin radix 路由在「`:id` 参数节点下两个同优先级静态子节点（renew/terminate）」叠加「`/contracts` 与 `/contract-templates` 前缀分裂」时，存在跨编译不确定的「已注册却不可达」缺陷；改为 DELETE 后 `:id` 不再有两个静态子节点，20/20 次构建稳定可达。后端 gin 已升级至 v1.12。详见 docs/07 工程经验。

### 2.6 档案库 archive

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET/POST/PUT | `/archive-categories` | 分类维护 | archive:admin |
| GET | `/archives?category=&employee_id=` | 档案列表 | archive:read |
| POST | `/archives` | 上传档案（附件） | archive:admin |
| POST | `/archives/{id}:borrow` | 发起借阅（走审批） | archive:borrow |
| POST | `/borrows/{id}:return` | 归还 | archive:borrow |
| GET | `/borrows?status=` | 借阅单列表/我的借阅 | archive:read |

### 2.7 任职奖惩 performance

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET/POST | `/qualifications` | 任职资格 | qualification:read/write |
| GET/POST | `/promotions` | 晋升（可走审批，联动职级） | reward:write |
| GET/POST | `/rewards-punishments` | 奖惩录入/查询 | reward:write |
| GET/POST | `/appraisals:import` | 考核结果导入归档 | reward:write |

### 2.8 员工关怀 care

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET/POST/PUT | `/care-rules` | 关怀规则 | care:write |
| GET | `/care/upcoming?month=` | 当月生日/周年/节日名单 | care:read |
| GET/POST | `/care-records` | 关怀记录（标记已关怀） | care:write |
| GET | `/care/stats` | 关怀统计 | care:read |

### 2.9 统计分析 analytics（✅ 全部已实现，结果按 org_path 子树收敛）

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/analytics/overview?scope=` | 工作台/看板汇总指标卡 | stat:read |
| GET | `/analytics/structure?dim=` | 人员结构（按 dept/position/level/gender/edu/age） | stat:read |
| GET | `/analytics/turnover?from=&to=` | 入离职趋势（按月） | stat:read |
| GET | `/analytics/tenure` | 司龄分布 | stat:read |
| GET | `/analytics/headcount` | 编制 vs 实有 | stat:read |

### 2.10 人事报表 report

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET/POST/PUT | `/report-templates` | 报表模板 | report:read |
| POST | `/reports:generate` | 生成报表（预览/导出） | report:export |
| GET | `/reports/{job_id}/download?format=excel\|pdf` | 下载 | report:export |
| GET/POST | `/report-jobs` | 定时任务（cron）配置/列表 | report:export |

### 2.11 人事动态 / 操作日志 timeline + audit

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/timeline?employee_id=&type=&from=&to=` | 人事动态时间线（hr_events） | employee:read |
| GET | `/audit-logs?user=&path=&from=&to=` | 操作日志检索 | audit:read |
| GET | `/audit-logs:export` | 导出日志 | audit:read |

### 2.12 系统设置 admin

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET/POST/PUT | `/roles` | 角色与权限点 | admin:role |
| GET/POST/PUT | `/dicts/{category}` | 数据字典 | admin:dict |
| GET/POST/PUT | `/reminder-rules` | 提醒规则（合同/生日/转正） | admin:config |
| GET/POST/PUT | `/users` | 账号管理（启停/绑定员工/改角色） | admin:role |

---

## 3. 错误码规范

| 区间 | 含义 | 示例 |
| --- | --- | --- |
| 0 | 成功 | `0 ok` |
| 1000–1099 | 通用/参数 | `1001 参数错误` `1002 校验失败` `1003 资源不存在` `1004 资源冲突` |
| 1100–1199 | 鉴权 | `1101 未登录` `1102 token 失效` `1103 无权限` `1104 数据范围越权` |
| 1200–1299 | 业务-流程 | `1201 流程状态不允许此操作` `1202 非当前审批人` `1203 流程已生效` |
| 1300–1399 | 业务-数据 | `1301 组织有下属不可删` `1302 工号重复` `1303 合同期限非法` |
| 1400–1499 | 导入导出 | `1401 模板不匹配` `1402 行级校验失败(附详情)` |
| 5000+ | 系统错误 | `5000 内部错误` `5001 依赖不可用` |

> 导入类错误在 `data` 内附行级明细：`{ "errors": [{"row":3,"field":"phone","msg":"格式错误"}], "success": 47, "failed": 3 }`。

---

## 4. 约定细节

- **幂等**：`:action` 类写操作对同一状态重复调用需幂等（已审批再审批返回 `1201`/幂等成功）。
- **分页上限**：`page_size` 上限 100；导出走专用接口（流式/异步），不走普通分页。
- **软删语义**：DELETE = 软删（置 `deleted_at`）；列表默认过滤已删。
- **批量**：批量操作返回每条结果数组，整体 207 风格的 `{success,failed,details}`。
- **版本演进**：破坏性变更升 `/api/v2`，本期只用 `v1`。
- **OpenAPI**：实现期补 `openapi.yaml`，作为前后端契约与 mock 来源。

---

## 变更记录

| 版本 | 日期 | 作者 | 说明 |
| --- | --- | --- | --- |
| v0.1 | 2026-06-10 | XXX | 初稿：统一约定、12 模块端点清单、错误码、约定细节 |
| v0.2 | 2026-06-10 | XXX | 对齐实现：process 动作改子路径风格、标注已实现端点（process/analytics） |
