# 05 · 视觉与交互设计

> 版本 v0.2 ｜ 创建 2026-06-10 ｜ 最后编辑 2026-06-11 ｜ 状态 已落地 ｜ 作者 XXX
>
> 信息架构与页面清单见 [01-product-design.md](01-product-design.md)。本文确定**设计语言与落地方式**。

---

## 1. 设计方法：用 `huashu-design` 定方向，Nuxt 落生产

仓库已安装 `huashu-design`（花叔Design）技能（`.agents/skills/huashu-design/`）。它擅长**高保真 HTML 原型、设计方向探索、幻灯片与评审**，但**显式声明不适合直接做生产级 Web App**。因此本项目按「**原型在前、生产在后**」两段式：

1. **设计方向探索（huashu-design 产出）**：用其「三套逻辑并行 → 出 3 版真实视觉」能力，针对核心页面（工作台 / 花名册 / 员工档案 / 组织架构图 / 审批详情 / 统计看板）产出高保真 HTML 原型与配色方案，规避 AI slop。
2. **设计语言固化**：从选定方向中抽取**设计 token**（色板/字阶/间距/圆角/阴影/动效），写入本文 §3。
3. **生产落地（Nuxt + Tailwind）**：把 token 映射为 Tailwind 主题与 CSS 变量，按 §5 组件清单实现，原型仅作视觉对照，不直接搬运到生产代码。

> 调用方式：在本仓库对 agent 说「用 huashu-design 给『花名册列表』做 3 个设计方向的高保真原型」即可触发；产物（HTML/截图）建议存放 `docs/design/prototypes/`，并在本文登记。

### 触发清单（建议用 huashu-design 产出的原型）

| # | 页面 | 目的 |
| --- | --- | --- |
| P1 | 工作台 Dashboard | 指标卡 + 待办 + 提醒 + 动态流的信息密度与节奏 |
| P2 | 员工花名册列表 | 大表格 + 多维筛选 + 自定义列的可用性 |
| P3 | 员工档案详情 | 头部信息卡 + 多 Tab 时间线 |
| P4 | 组织架构图 | 树/图可视化的视觉表达 |
| P5 | 审批流详情 | 表单 + 审批轨迹时间线 |
| P6 | 统计分析看板 | ECharts 图表网格的排布与配色 |

---

## 2. 设计基调（Design Tone）

面向 HR / 管理者的**专业、可信、克制**的企业级后台。关键词：**清晰的信息层级、低饱和主色、充裕留白、数据可读**。避免：渐变滥用、玻璃拟态堆砌、霓虹色、无意义动效（即 huashu-design 强调的 anti-AI-slop）。

- **气质**：像「成熟 HR SaaS」（参考 Linear/Notion 的克制 + 企业后台的信息密度），而非消费级炫技。
- **中文优先排版**：以中文字号/行高为基准，数字与英文用等宽或 tabular-nums 对齐。

---

## 3. 设计 Token（v0.2 已落地，实现见 `web/uno.config.ts`）

> 实际以 **UnoCSS（presetWind4）theme + shortcuts** 落地：主题色阶定义在 `web/uno.config.ts` `theme.colors`，组件级样式收敛为 shortcuts（`app-card / btn-primary / btn-secondary / btn-danger / input-base / th-base / td-base / row-base / link-action / seg-*`），页面只引用 token，不手写裸样式。

### 3.0 落地差异速览（v0.2）

- **主色**：`primary #2f54eb`（完整 50–950 色阶；hover 用 `primary-700` 实色，不再用透明度 hover）。
- **品牌深色**：新增 `ink` 色阶（深墨蓝 `#151a26`），用于全局深色侧栏与登录页品牌面板；品牌字标「OrgHR」用 DM Serif Display。
- **中性色**：presetWind4 无 `truegray`，在 theme 显式补全（≈neutral）；边框统一用 `black/4~10` 发丝线，分割更轻。
- **语义徽标**：green→emerald、red→rose、blue→primary 色系，徽标加同色系 ring 描边 + 可选状态圆点（`HBadge dot`）。
- **数字排版**：统计大数用 `font-serif`（DM Serif Display）、表格/日期数值用 `.tnum`（tabular-nums，定义于 `main.css`）。
- **图标**：iconify carbon 集合需在 `presetIcons.collections` 显式注册（自定义 collections 会关闭自动解析）；`nav.ts` 中以字符串声明的导航图标走 `safelist`。
- **组件**：新增 `HPageHeader`（标题/描述/返回/动作）、`HEmpty`（图标空态）、`HStat`（指标卡）；导航配置集中 `app/utils/nav.ts`（分组：总览/组织与人员/事务与流程/洞察与报表）。

### 原 v0.1 占位基线（已被上述落地值取代，保留备查）

### 3.1 色彩

| Token | 值（占位） | 用途 |
| --- | --- | --- |
| `--color-primary` | `#2F5BEA`（沉稳蓝） | 主操作、链接、选中 |
| `--color-primary-weak` | `#EAF0FF` | 主色浅底 |
| `--color-success` | `#1F9D5B` | 在职/通过 |
| `--color-warning` | `#D98B12` | 即将到期/试用 |
| `--color-danger` | `#D93636` | 离职/驳回/删除 |
| `--color-info` | `#5B7083` | 中性提示 |
| `--color-fg` / `--color-fg-muted` | `#1A2230` / `#5B6675` | 正文/次要文字 |
| `--color-bg` / `--color-surface` | `#F6F8FB` / `#FFFFFF` | 页底/卡片面 |
| `--color-border` | `#E3E8EF` | 分割线/描边 |

> 语义色用于**状态徽标**（Badge）统一映射：在职=success、试用=warning、离职=danger、待审批=info、已通过=success、已驳回=danger。

### 3.2 字阶 / 间距 / 形状

- 字号：`12 / 13 / 14(正文) / 16 / 20 / 24 / 30`，行高 1.5；标题用 600，正文 400。
- 间距基准 `4px` 栅格：`4 / 8 / 12 / 16 / 24 / 32`。
- 圆角：控件 `8px`、卡片 `12px`、弹层 `12px`。
- 阴影：克制两级 —— `sm`（卡片）、`md`（弹层/抽屉）。
- 动效：`150–200ms ease`；仅用于状态反馈与层级进出，不做装饰性动画。

### 3.3 布局栅格

- 整体「左侧固定导航（可收起）+ 顶部栏 + 内容区」。内容区最大宽度自适应，列表页两栏（筛选 + 表格），详情页头部卡 + Tab。
- 断点：`sm 640 / md 768 / lg 1024 / xl 1280`；后台主用 `lg+`，移动端只保证可读（非重点）。

---

## 4. 关键页面布局（线框）

**工作台**
```
┌ 顶栏：Logo  搜索  通知  用户 ───────────────┐
│ 指标卡区：在职人数 | 本月入职 | 本月离职 | 待办  │
│ ┌ 待办/审批 ─┐ ┌ 提醒(生日/合同/转正) ─┐        │
│ │ 列表        │ │ 列表                  │        │
│ └────────────┘ └──────────────────────┘        │
│ 最近人事动态（时间线）                          │
└────────────────────────────────────────────────┘
```

**花名册列表**
```
左侧导航 | ┌ 筛选条（组织/部门/岗位/职级/状态/入职时间/关键字）┐
         | │ 工具条：自定义列 ▸  导入  导出  新增                │
         | │ 表格（可冻结/排序/分页/批量选择）                  │
         | └────────────────────────────────────────────────┘
```

**员工档案详情**
```
头部信息卡（头像/姓名/工号/部门/岗位/职级/状态 + 快捷操作）
Tabs：基本信息 | 任职履历 | 合同 | 档案 | 奖惩 | 关怀 | 动态时间线
```

**审批详情**
```
左：流程表单（变更前→后对比）   右：审批轨迹时间线（步骤/审批人/意见/时间）
底部操作：通过 / 驳回 / 撤销
```

---

## 5. 组件清单（与代码对齐）

**基础组件 `components/ui/`**：Button、Input、Select、DatePicker、Checkbox/Radio、Tag/Badge、Avatar、Card、Modal、Drawer、Tabs、Table（含列配置）、Pagination、Tree、Steps/Timeline、Toast、EmptyState、Skeleton、Tooltip、Dropdown、Breadcrumb。

**业务组件 `components/biz/`**：OrgTree（组织/部门树）、OrgChart（架构图）、EmployeeTable（花名册大表）、EmployeeCard（档案头卡）、ApprovalTimeline（审批轨迹）、StatCard（指标卡）、ChartPie/ChartBar/ChartLine（ECharts 封装）、ImportDialog（导入向导）、ColumnSetting（自定义列）、ReminderList（提醒）。

**状态映射约定**：所有枚举（在职状态/流程状态/合同状态…）集中在 `utils/enums.ts`，统一中文文案 + Badge 语义色，避免散落硬编码。

---

## 6. 交付与登记

- 原型产物路径：`docs/design/prototypes/`（HTML/截图），每个原型在下表登记。
- 设计 token 最终值在 §3 落定后，同步到 `web/assets/css/theme`。

| 原型 | 文件 | 版本 | 状态 |
| --- | --- | --- | --- |
| （待 huashu-design 产出后登记） | — | — | 计划中 |

---

## 变更记录

| 版本 | 日期 | 作者 | 说明 |
| --- | --- | --- | --- |
| v0.1 | 2026-06-10 | XXX | 初稿：设计方法（huashu-design 两段式）、基调、token 基线、关键页线框、组件清单 |
| v0.2 | 2026-06-11 | XXX | 视觉升级落地：primary/ink 色阶 + UnoCSS shortcuts 设计 token、深色分组侧栏、登录页品牌面板、HPageHeader/HEmpty/HStat 组件、衬线数字与 tabular-nums、徽标语义色板（emerald/rose/primary + ring）；修复 truegray/carbon 图标静默失效两处系统性问题 |
