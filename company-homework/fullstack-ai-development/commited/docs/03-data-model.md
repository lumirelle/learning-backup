# 03 · 数据模型

> 版本 v0.1 ｜ 创建 2026-06-10 ｜ 最后编辑 2026-06-10 ｜ 状态 草稿 ｜ 作者 XXX
>
> 本文是**数据单一事实源（SSOT）**。字段为规划级，落库以 migrations 为准。约定沿用 [02-architecture.md](02-architecture.md) §6。

---

## 1. 通用约定

- **主键**：`id BIGINT`（Snowflake），除纯日志表用 `BIGSERIAL`。
- **审计列**：`created_at / updated_at TIMESTAMPTZ DEFAULT NOW()`，软删 `deleted_at TIMESTAMPTZ NULL`。
- **树**：用 materialized path `path VARCHAR(512)`（如 `/g/sub_a/dept_eng`），配 `parent_id` 与 `sort_order`。
- **多组织**：业务表带 `org_id` + `org_path`，用于数据权限（`path LIKE '<scope>%'`）。
- **半结构化**：可变/分组字段用 `JSONB`，需检索的建 GIN 索引。
- **枚举**：用 `VARCHAR` + 应用层常量（附 §9 枚举字典），不滥用 PG enum 以便演进。
- **金额/日期**：金额 `NUMERIC(12,2)`；纯日期 `DATE`，时点 `TIMESTAMPTZ`。

---

## 2. ER 总览

```
organizations ──< departments ──< positions
        │              │
        │              └── head_user_id ─┐
        ▼                                │
     employees ─────────────────────────┘
        │  (主数据/人事档案)
        ├──< employee_job_history     任职履历（调动/转正快照）
        ├──< contracts ──> contract_templates
        ├──< archive_items ──< archive_borrows      （archive_categories 分类）
        ├──< qualifications / promotions / rewards_punishments / appraisals
        ├──< care_records              （care_rules 规则）
        └──< hr_events                 人事动态时间线

hr_processes (入离调转/转正单) ──1:1──> approval_instances ──< approval_steps
        └── 生效后写 employees / employee_job_history / hr_events

job_levels        职级序列（独立字典）
report_templates ──< report_jobs
audit_logs        接口级操作日志（中间件）
users / roles     账号与角色（鉴权）；users 与 employees 解耦（见 §3.4）
data_dicts        数据字典（学历/民族/合同类型/离职原因…）
user_list_prefs   用户列表列偏好
employee_field_defs  可配置员工字段
```

---

## 3. 核心表设计

> 仅列关键列；`created_at/updated_at/deleted_at` 默认存在不再赘述。

### 3.1 组织域

**organizations（公司主体树）** — 沿用参考工程
```
id, parent_id, code(uniq), name, short_name,
type           -- group|subsidiary|bu
path, sort_order, status(active|disabled), description
```

**departments（部门树）**
```
id, org_id→organizations, parent_id→departments, code, name, short_name,
path, sort_order, head_user_id→employees, status, description
uniq(org_id, code)
```

**positions（岗位）**
```
id, org_id, dept_id→departments, code, name,
job_family       -- 职位族：技术/产品/职能…
level_seq        -- 关联职级序列 code（如 'P')
headcount        -- 编制数
status, description
uniq(dept_id, code)
```

**job_levels（职级体系）**
```
id, seq_code      -- 'P' | 'M'
name              -- 专业序列 / 管理序列
level_code        -- 'P5' | 'M3'
level_order       -- 排序权重
description
uniq(seq_code, level_code)
```

### 3.2 员工主数据（花名册 + 档案）

**employees（人事档案主表）** — 核心实体
```
id,
employee_no(uniq)          工号
user_id→users (nullable)   关联登录账号（解耦，见 3.4）
name, en_name, gender, birthday(DATE), avatar,
id_card_no(脱敏), id_card_type, nationality, native_place,
marital_status, political_status, education, degree,
phone, personal_email, work_email, address(JSONB),
emergency_contact(JSONB),  紧急联系人
-- 组织归属（冗余路径便于查询/数据权限）
org_id, org_path, dept_id, dept_name, position_id, position_name,
job_level,                 当前职级 P6/M2
manager_id→employees,      直属上级（汇报关系）
-- 雇佣信息
employment_type            full_time|part_time|intern|outsource
employment_status          probation|active|leaving|left   在职状态
hired_at(DATE), regular_at(DATE), left_at(DATE),
work_years_base(DATE),     司龄起算（连续工龄）
extra(JSONB)               可配置扩展字段（配合 employee_field_defs）
索引: org_path(gin/btree), dept_id, position_id, employment_status, name(trgm)
```

**employee_job_history（任职履历快照）**
```
id, employee_id→employees,
change_type        onboard|transfer|promote|regularize|leave
from(JSONB), to(JSONB)     变更前后的 org/dept/position/level/manager 快照
process_id→hr_processes,   来源流程单
effective_date(DATE), remark
```

**employee_field_defs（可配置字段）**
```
id, key, label, group, type(text|number|date|select|file),
options(JSONB), required, sort_order, pii(bool), enabled
```

**user_list_prefs（列表列偏好，用户级）**
```
id, user_id, list_key(如 'roster'), columns(JSONB), filters(JSONB)
uniq(user_id, list_key)
```

### 3.3 人事流程与审批

**hr_processes（统一人事流程单：入离调转/转正）**
```
id, process_no(uniq), type        onboard|offboard|transfer|regularize
employee_id (nullable for onboard 拟入职), applicant_id→users,
org_id, org_path,
payload(JSONB)        各类型表单数据（拟入职信息/目标岗位/离职原因/最后工作日…）
status                draft|pending|approved|rejected|effective|cancelled
effective_date(DATE), result(JSONB)
approval_id→approval_instances
索引: type, status, employee_id, applicant_id
```

**approval_instances（通用审批实例）**
```
id, biz_type            process|borrow|promotion|contract...
biz_id                  对应业务单 id
status                  pending|approved|rejected|cancelled
current_step, total_steps,
submitted_by→users, submitted_at
```

**approval_steps（审批步骤）**
```
id, instance_id→approval_instances, step_no,
approver_id→users, approver_role,
action                  pending|approved|rejected|transferred
comment, acted_at
```

### 3.4 账号与权限

> **解耦设计**：`users`（可登录账号 + 鉴权）与 `employees`（人事档案）分离，一个员工可有 0/1 个账号；离职员工档案保留但账号停用。参考工程的 `users` 偏「账号」，本项目把「人」的全量信息下沉到 `employees`。

**users（登录账号）** — 裁剪自参考工程
```
id, username(uniq), name, password_hash, employee_id→employees(nullable),
email, phone, avatar, org_id, org_path,
roles(JSONB), is_admin, is_active, last_login_at
gin(roles)
```

**roles（角色目录）**
```
id, code(uniq), name, description, is_builtin, perms(JSONB)  权限点集合
```

**data_dicts（数据字典）**
```
id, category(如 'education'|'leave_reason'|'contract_type'),
code, label, sort_order, enabled
uniq(category, code)
```

### 3.5 档案库

**archive_categories**：`id, parent_id, name, code, path, sort_order`
**archive_items**
```
id, employee_id→employees, category_id→archive_categories,
title, file_meta(JSONB)  文件名/大小/类型/存储key, storage_ref,
is_borrowable(bool), status  in_stock|borrowed, security_level
```
**archive_borrows（借阅单，状态机）**
```
id, item_id→archive_items, borrower_id→users,
reason, status  pending|approved|borrowed|returned|overdue|rejected,
approval_id→approval_instances, due_date(DATE), borrowed_at, returned_at
```

### 3.6 合同

**contract_templates**：`id, name, type, content(TEXT/JSONB), enabled`
**contracts**
```
id, contract_no(uniq), employee_id→employees, template_id→contract_templates,
type           fixed_term|open_ended|intern|labor_dispatch
status         draft|active|expiring|expired|terminated|renewed
sign_date(DATE), start_date(DATE), end_date(DATE),
prev_contract_id→contracts(续签链), salary_band, terms(JSONB)
索引: employee_id, status, end_date
```

### 3.7 任职奖惩

**qualifications**：`id, employee_id, name, level, granted_at, expire_at, evidence(JSONB)`
**promotions**：`id, employee_id, from_level, to_level, effective_date, process_id, reason`
**rewards_punishments**
```
id, employee_id→employees, kind  reward|punishment,
category, title, reason, amount(NUMERIC,nullable),
effective_date(DATE), attachment(JSONB), recorded_by→users
```
**appraisals**：`id, employee_id, period(如 '2026H1'), result, score, summary, archived_at`

### 3.8 员工关怀

**care_rules**：`id, type birthday|anniversary|festival, name, advance_days, template(TEXT), enabled`
**care_records**
```
id, employee_id→employees, rule_id→care_rules, type,
care_date(DATE), method  message|gift|card, status pending|done,
remark, operator_id→users
```

### 3.9 统计 / 报表

- 统计分析主要用**聚合查询/视图**（无核心新表）；可加**物化视图** `mv_headcount_by_dept` 等并定时刷新。
**report_templates**：`id, name, type, columns(JSONB), filters(JSONB), group_by(JSONB), format excel|pdf, enabled`
**report_jobs**：`id, template_id, params(JSONB), schedule(cron, nullable), status, last_run_at, output_ref, created_by`

### 3.10 动态与审计

**hr_events（人事动态时间线）**
```
id, employee_id→employees(nullable for org级事件),
event_type   onboard|transfer|promote|regularize|leave|contract|reward|care|org_change,
title, detail(JSONB), org_id, org_path,
actor_id→users, occurred_at(TIMESTAMPTZ)
索引: employee_id, event_type, occurred_at
```

**audit_logs（接口级操作日志）** — 沿用参考工程（中间件写）
```
id BIGSERIAL, user_id, username, method, path, status, ip,
request_id, body(摘要/脱敏), created_at
索引: user_id, created_at, path
```

> **动态 vs 审计**：`hr_events` 是**业务语义**事件（给 HR 看「发生了什么人事变动」）；`audit_logs` 是**技术留痕**（给审计看「谁调了哪个接口」）。二者互补，§01 域 10 已说明。

---

## 4. Migrations 规划（顺序）

| 序号 | 文件 | 内容 |
| --- | --- | --- |
| 0001 | `organizations.sql` | 组织主体树 |
| 0002 | `departments.sql` | 部门树 |
| 0003 | `positions.sql` | 岗位 |
| 0004 | `job_levels.sql` | 职级序列 |
| 0005 | `users.sql` | 登录账号 |
| 0006 | `roles.sql` | 角色目录 + 权限点 |
| 0007 | `data_dicts.sql` | 数据字典 |
| 0008 | `employees.sql` | 人事档案主表（+ trgm/gin 索引） |
| 0009 | `employee_job_history.sql` | 任职履历 |
| 0010 | `employee_field_defs.sql` / `user_list_prefs.sql` | 字段配置/列偏好 |
| 0011 | `approval.sql` | approval_instances / approval_steps |
| 0012 | `hr_processes.sql` | 入离调转/转正单 |
| 0013 | `contracts.sql` | 合同 + 模板 |
| 0014 | `archives.sql` | 分类/条目/借阅 |
| 0015 | `performance.sql` | 资格/晋升/奖惩/考核 |
| 0016 | `care.sql` | 关怀规则/记录 |
| 0017 | `reports.sql` | 报表模板/任务 |
| 0018 | `hr_events.sql` | 人事动态 |
| 0019 | `audit_logs.sql` | 操作日志 |
| 0020 | `indexes_and_views.sql` | 跨表索引、统计物化视图 |
| 0099 | `seed.sql` | 种子：组织树/角色/字典/演示账号/造数（亦可由 cmd/seed 程序写） |

> 迁移**只增不改**：上线后的结构变更新增 `00xx_alter_*.sql`，不回改历史文件。

---

## 5. 种子数据（seed）规划

- 1 集团 + 2 子公司 + 6 部门 + 若干岗位；P/M 两条职级序列。
- 6 类角色 + 权限点映射；4 个演示账号（见 `01` §7）。
- 数据字典：学历、婚姻、政治面貌、合同类型、离职原因、奖惩类别等。
- 造数：~120 名员工（覆盖在职/试用/离职、各部门/职级/性别/学历/入职年份分布），用于检索与统计看板演示。

---

## 6. 数据一致性与事务边界

- 流程**生效**是一个事务：更新 `employees` + 写 `employee_job_history` + 写 `hr_events` + 更新 `hr_processes.status`，全成功或全回滚。
- 组织/部门**移动**：在事务内级联更新子树 `path`（`UPDATE ... WHERE path LIKE '<old>%'` 替换前缀）。
- 软删不物理删除，保证档案与履历可追溯；唯一索引需带 `WHERE deleted_at IS NULL` 的部分索引。

---

## 变更记录

| 版本 | 日期 | 作者 | 说明 |
| --- | --- | --- | --- |
| v0.1 | 2026-06-10 | XXX | 初稿：通用约定、ER、10 域核心表、migrations 与 seed 规划、一致性 |
