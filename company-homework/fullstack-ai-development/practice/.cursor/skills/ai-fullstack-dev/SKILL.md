---
name: ai-fullstack-dev
description: AI 辅助全栈开发工作流。用于从 0 到 1 搭建中等复杂度 Web 系统 MVP，覆盖选题分析、需求调研、技术设计、编码实现到部署交付的完整流程。当用户提到全栈开发、搭建系统、需求文档、MVP、从零开始建项目时触发。
metadata: v0.1.0.20260527
---

# AI 全栈开发工作流

## 阶段总览

```
Phase 1: 调研  →  Phase 2: 技术设计  →  Phase 3: 编码实现  →  Phase 4: 交付
```

---

## 注意事项

### 及时更新根脚本转发

及时更新根脚本转发，方便用户操作项目，如：

```json
{
  "scripts": {
    "fe:dev":       "cd ats-frontend && bun run dev",
    "fe:typecheck": "cd ats-frontend && bun run typecheck",
    "fe:lint":      "cd ats-frontend && bun run lint",
    "fe:check":     "cd ats-frontend && bun run typecheck && bun run lint",
    "be:dev":       "cd ats-backend && mvn spring-boot:run",
    "be:test":      "cd ats-backend && mvn -B test",
    "be:build":     "cd ats-backend && mvn -B package -DskipTests"
  }
}
```

## Phase 1：需求调研

### 1.1 需求调研

输入：候选系统（如白板/文档）

输出：基于选定系统，完成以下内容并输出 HTML 文档：

1. **概述**
   1. **项目背景**：项目背景即参考产品
   2. **目标与范围**：项目目标，迭代周期以及个周期交付范围
   3. **角色与权限**：列出所有用户角色及对应操作权限
2. **功能需求**
   1. 功能用例表。每个模块列出用例编号、名称、角色、描述，可以扩展涉及的概念定义
3. **技术设计**
   1. **状态机设计**：核心业务实体的状态流转图与约束规则
   2. **数据模型（ERD）**：列出所有核心表、字段、类型、主外键关系
   3. **API 设计**：RESTful 接口列表清单，含方法、路径、权限要求
   4. **技术架构选型**：前后端框架和库、数据库、缓存、文件存储、部署方案等
4. **其他**
   1. **非功能需求**：性能、安全、兼容性基线
   2. **MVP 页面清单**：前端路由列表，标注角色可见范围
   3. **开发里程碑**：按天拆分前后端并行任务

按需扩充。

输出路径：`docs/project-requirements.html`

---

## Phase 2：技术设计

输出：

| 名称 | 路径 | 说明 |
|---|---|---|
| 数据库 DDL | `docs/project-schema.sql` | PostgreSQL 建表语句 |
| 技术设计文档 | `docs/project-tech-design.html` | 后端/前端结构可视化 HTML |

### 2.1 数据库 Schema（`docs/project-schema.sql`）

用 SQL DDL 输出建表语句（PostgreSQL 语法）

包含：

- 枚举类型（`CREATE TYPE`）
- 所有表的字段、类型、NOT NULL、DEFAULT、CHECK 约束
- 主外键关系，ON DELETE 策略按业务选 CASCADE / RESTRICT / SET NULL
- B-tree 索引（高频查询字段）；文本搜索用 GIN 索引
- 每张表和关键字段的 `COMMENT`
- `updated_at` 自动更新触发器（统一用 `set_updated_at()` 函数）
- 常用聚合视图（如漏斗、月度概览）
- 种子数据（初始管理员账号、字典数据）

按需扩充。

### 2.2 技术设计文档（`docs/project-tech-design.html`）

1. **概述**
   1. 系统架构概览：前端 ↔ 后端 ↔ 数据库/缓存/文件 的分层架构图
2. **数据库**：
   1. Schema 说明：核心表用途、数据量级、关键约束速查表
   2. 枚举类型清单
   3. 索引策略说明
3. **后端**：
   1. 项目结构文件树（暗色背景，含目录注释）
   2. 各层（Controller / Service / Repository / Model / Config / Exception）职责卡片
   3. 编码规范（响应格式、错误码、状态机约束、安全规范）
   4. 环境变量清单（`.env.example` 内容）
4. **前端**：
   1. 项目结构文件树（含 views、components、stores、api、router、utils、types 目录注释）
   2. 各模块职责卡片
   3. 编码规范（组件风格、状态管理、加载处理、代码质量）
   4. 环境变量清单
5. **部署**
   1. Docker Compose 部署方案：服务表（postgres / redis / backend / frontend/nginx）
   2. 开发启动检查清单：后端 + 前端各自的启动前核查项

按需扩充。

### 2.3 后端分层参考

Spring Boot 推荐分层：

```
controller/   → 接口层，参数校验（@Valid）
service/      → 业务逻辑，事务管理，状态机校验
repository/   → 数据访问（MyBatis-Plus 或 JPA）
model/entity/ → 数据库实体
model/dto/    → 请求入参（含校验注解）
model/vo/     → 响应出参（脱敏）
config/       → JWT、Security、Redis、CORS、文件上传配置
middleware/   → JWT 过滤器（注入 SecurityContext）
exception/    → 全局异常处理（@RestControllerAdvice）
```

Gin 推荐分层：

```
handler/    → 路由处理
service/    → 业务逻辑
repo/       → 数据库访问（sqlx 或 GORM）
model/      → 结构体（entity / dto / vo）
middleware/ → JWT、CORS、日志
```

按需扩充。

### 2.4 前端结构参考

```
src/
  views/       → 页面组件（与路由 1:1，HR 页面放 views/hr/ 子目录）
  components/  → 通用组件（KanbanCard、StageTimeline、FunnelChart 等）
  stores/      → Pinia 状态（useAuthStore 持久化 Token）
  api/         → request.ts（Axios 实例+拦截器）+ 各模块请求函数
  router/      → 路由配置（meta.roles 声明权限）+ 全局路由守卫
  utils/       → token.ts / format.ts / stageMap.ts（枚举→标签/颜色）
  types/       → api.d.ts（与后端 VO 对齐）+ enums.ts
  styles/      → tokens.css（design tokens）+ global.css
```

按需扩充。

### 2.5 UI 设计基线

> 参考 awwwards 优秀作品。中等复杂度 Web 系统通常需要**两种语言并存**：
> - **张扬轨**（Stripe / Resend / Linear 营销页系）— 用于 Hero / Landing / 营销页：大渐变、巨型字号、aurora 漂移、噪点质感、强 accent 色、大胆微动画
> - **克制轨**（Linear app / Vercel Dashboard / Cron / Notion 系）— 用于应用内（列表、看板、表单、详情）：极致可读性、信息密度、克制配色、微动画
>
> **统一 tokens，分场景配方**：颜色不能 hardcode，全部走 CSS variables；张扬轨在克制 tokens 基础上**追加**(而非替换) accent / gradient / glow / noise token。

#### 2.5.1 共用 tokens 基线

**色彩**：

- **品牌主色**：单一品牌色，选择**贴合项目语义**的颜色
- **中性色 9-11 阶灰度**（`gray-50` … `gray-950`），背景、边框、文字层级全靠它
- **语义色**：success / warning / danger / info 各一组（base + bg + border）
- **accent palette（张扬专用）**：围绕品牌叙事挑选 4-6 色构造 mesh 渐变
- **暗色模式预留 token 切换位**

> ⚡ **色彩叙事原则**：accent palette 的颜色顺序应能映射到项目的核心业务流（如招聘漏斗 stage、订单状态、用户成长等级）。这样列表 / 看板 / 状态徽章用到对应阶段时，颜色复用且语义自洽，不需要二次设计。

**排版**：

- 中文：`PingFang SC` / `Microsoft YaHei` / `Noto Sans SC`；英文/数字：`Inter` / `JetBrains Mono`
- 克制字号 6 档：`12 / 13 / 14 / 16 / 20 / 24`；张扬巨字号 3 档：`clamp(48,8vw,80) / (64,11vw,128) / (80,14vw,160)`
- 标题字重 600/700；张扬 hero 800；正文 400；重要数字 500–600 + tabular-nums

**圆角 / 间距 / 阴影**：

- 8px 栅格；圆角 6 / 8 / 12 三档；张扬 CTA 用 `999px` full
- 阴影 3 档（`shadow-sm/md/lg`），张扬场景额外加 `glow-brand/magenta/violet` 大模糊光晕
- 卡片白底 + 1px 浅边框；hover 升 2-4px + `shadow-lg`

#### 2.5.2 张扬轨追加武器

```css
/* mesh gradient（背景 / 文字 fill） */
--grad-aurora: radial-gradient(...) + 3 个 blob;
--grad-sunrise: linear-gradient(135deg, magenta, violet, cyan);
--grad-fire:    linear-gradient(135deg, amber, magenta, violet);

/* 光晕（hero CTA / 焦点元素） */
--glow-brand / --glow-magenta / --glow-violet

/* 噪点 SVG overlay（叠在大色块上抗 banding，加质感） */
--noise-url: url("data:image/svg+xml;..." feTurbulence ...)

/* aurora 漂移动画 */
@keyframes aurora-drift { ... } /* 24s 缓慢有机感 */

/* 入场 reveal */
.reveal { opacity: 0; transform: translateY(16px); transition: ... }
.reveal.is-visible { ... }

/* cursor follow glow（鼠标跟随发光） */
.cursor-glow { background: radial-gradient(400px circle at var(--mx) var(--my), ...) }
```

实用 utility class：`text-gradient` / `text-gradient-fire` / `with-noise` / `aurora-layer` / `reveal`。

**强约束**：

- `prefers-reduced-motion: reduce` 必须关闭 aurora 与 reveal
- 张扬 hero 仅出现在 landing/marketing 页，**应用内主流程**（看板、列表、表单）保持克制
- 渐变文字必须配 `-webkit-background-clip + background-clip + color: transparent`，保证 Safari/Chrome 双兼容

#### 2.5.3 UI 组件库使用

- 选择支持主题注入的组件库，把我们自己设计的 token 注入进去
- 自定义组件用 CSS variables（`var(--brand-500)` 等）跟随主题切换
- 元素**不必**强行套组件，原生的组件可以支持设计更华丽的界面效果

#### 2.5.4 样式工具：UnoCSS · Attributify 风格第一公民（强制）

**所有前端项目使用 UnoCSS 作为原子化 CSS 引擎**，且优先用 **attributify 风格** 而不是 class 风格。理由：

1. Attribute 风格支持同 prefix 合并 → 可读性远超 class 风格，字符总量比 class 风格少 30-50%

##### 接入步骤

```bash
bun add -D unocss @unocss/preset-uno @unocss/preset-attributify @unocss/preset-icons \
              @unocss/transformer-directives @unocss/transformer-variant-group
```

```ts
// vite.config.ts
import Unocss from 'unocss/vite'

plugins: [Unocss(), Vue()]  // UnoCSS 必须在 Vue 之前

// main.ts
import 'virtual:uno.css'
import './styles/global.css'  // 设计 tokens + 复杂动画
```

##### Token 桥接

`uno.config.ts` 中 `theme.colors / spacing / fontSize / boxShadow / transitionTimingFunction / transitionDuration` 全部用 `var(--xxx)` 引用 `tokens.css` 里定义的变量，做到统一设计。

```ts
theme: {
  colors: {
    brand: { 500: 'var(--brand-500)', /* ... */ }, // 使用时可以直接 <div bg-brand-500>，不再需要 <div bg="(brand-500)">
    accent: { mint: 'var(--accent-mint)', emerald: 'var(--accent-emerald)', /* ... */ },
    // ...
  },
  transitionTimingFunction: {
    out: 'var(--ease-out)',           // hover 微交互
    'page-in': 'var(--ease-page-in)', // 页面/模态转场
    // ...
  },
  transitionDuration: {
    'page-in': 'var(--dur-page-in)',  // 与 2.5.4 速度感工程学绑定
    // ...
  },
  // ...
}
```

##### Attributify 风格落地规约（最容易踩坑的地方）

**核心选择树**（按这个优先级判断写哪种形式）：

```
1. 无值？                                                   → <div flex relative grid>
2. 单值（值为 keyword 或单位为 "spacing"） / shortcut？    → <div bg-app mt-8 rounded-full>
3. 单值，自定义单位？                                      → <div max-w-1200px gap-10px>  🚨 UnoCSS 支持自定义单位而无需中括号，和 Tailwind CSS 不同，可以绕过浏览器 Attribute name 不可带中括号的限制
4. 单值，CSS 变量？                                         → <div bg-brand-500 text-text-primary>  🚨 需确认所用变量是否在 theme 等处正确注册
5. 单值，复杂函数？                                        → <div max-w="[max(80vw,80%)]">  🚨 只能走 value 模式
6. 单值，和元素 / 组件属性冲突？                          → <div text-red>  🚨 不允许 value 模式，否则会影响元素属性值
7. 单值，复杂函数，又和元素 / 组件属性冲突？             → <div class="text-[clamp(28px,4vw,44px)]">  🚨 只能回退为 class 风格
8. 单值 variant（hover/focus/group-hover/max-sm）？         → <div hover:bg-active group-hover:translate-x-1>
9. 多值？                                                   → <div flex="~ items-center" p="y-3 x-4">  ✨ 聚合相关联的 utilities，减少重复前缀字符
```

> 💡 **借助插件做权威校验**：UnoCSS 写法的合法性以工具实时报告为准，**不要凭记忆判断**：
> - **VSCode**：装 `antfu.unocss` 扩展，hover / 行内即可看到 utility 是否匹配 + 生成的 CSS
> - **CLI 校验**：装 `@unocss/eslint-plugin`，配 minimal `eslint.config.js` 跑 `eslint . --rule '@unocss/order: error' --rule '@unocss/blocklist: error'` 验证；也可直接看 vite dev server stdout 的 `[unocss] unmatched utility "..."` 警告
> - **凡是 vite 报 unmatched 的写法表面用了不存在的规则 / 格式有误**——立即修，不要 ship 残缺 class

##### shims.d.ts 必备声明

```ts
declare module 'virtual:uno.css'
```

##### Shortcuts 分层规约（≥ 3 处复用就抽 shortcut）

**判断标准**：同一段 utility 组合在 ≥ 3 个组件出现，或单段 ≥ 40 字符 → 立即抽进 `uno.config.ts` 的 `shortcuts`，模板里只剩语义类名。

**命名前缀分组**（来自 M1 实战沉淀，扫一眼前缀就知道用途）：

| 前缀 | 用途 | 示例 |
|---|---|---|
| `layout-*` / `center-*` / `between-*` / `col-*` | 布局原子 | `center-flex`、`between-flex`、`col-flex`、`card-base` |
| `typo-*` / `heading-*` / `kicker` / `eyebrow` | 排版语义 | `heading-1`、`eyebrow`（uppercase + tracking）|
| `surface-*` | 容器（玻璃 / 卡片） | `surface-glass`、`surface-glass-dark`、`surface-elevated` |
| `btn-*` | 按钮变体（含 hover/active） | `btn-primary`、`btn-secondary`、`btn-cta` |
| `field-*` / `kbd-hint` / `error-banner` / `demo-*` | 表单原子 | `error-banner`、`error-icon`、`demo-card`、`kbd-hint` |
| `navbar-*` / `logo-*` / `avatar` / `user-trigger` / `version-pill` | 导航栏专属 | `navbar-glass`、`navbar-glow-line`、`logo-mark`、`logo-mark-lg` |
| `brand-*` / `hero-*` / `aurora-bg-*` / `orb-*` | 品牌视觉（认证页 / Hero） | `brand-pane`、`hero-display`、`hero-outline`、`hero-gradient` |
| `anim-*` / `animate-*` / `btn-shimmer` / `cta-glow` | 动画绑定 | `animate-shimmer`、`btn-shimmer`、`cta-glow`（搭配 `group` 用）|

##### Keyframes 集中沉淀策略

`@keyframes` 定义**只能写在 CSS** 里（UnoCSS 不支持定义，只能引用）。集中沉淀到 `styles/global.css`，shortcuts 通过 `animate-xxx` utility 引用：

```css
/* global.css —— 所有共享 keyframes 集中 */
@keyframes shimmer       { 0%{transform:translateX(-100%)} 50%,100%{transform:translateX(100%)} }
@keyframes gradient-flow { 0%,100%{background-position:0% 50%} 50%{background-position:100% 50%} }
@keyframes aurora-shift  { 0%,100%{transform:translate3d(0,0,0) scale(1)} 50%{transform:translate3d(-3%,2%,0) scale(1.05)} }
@keyframes orb-float-a   { 0%,100%{transform:translate3d(0,0,0)} 50%{transform:translate3d(40px,30px,0)} }
@keyframes card-bob      { 0%,100%{translate:0 0} 50%{translate:0 -6px} }
```

```ts
// uno.config.ts rules 里逐个注册 animate-* utility
rules: [
  ['animate-shimmer',       { animation: 'shimmer 2.5s ease-in-out infinite' }],
  ['animate-gradient-flow', { animation: 'gradient-flow 6s ease-in-out infinite' }],
  ['animate-aurora-shift',  { animation: 'aurora-shift 18s ease-in-out infinite' }],
  // ...
]
```

```css
@media (prefers-reduced-motion: reduce) {
  [class*="animate-aurora"],
  [class*="animate-orb"],
  [class*="animate-gradient"],
  [class*="animate-card-bob"],
  [class*="animate-tag-float"],
  [class*="animate-shimmer"],
  [class*="animate-pulse"] { animation: none !important; }
}
```

##### text-stroke / 复杂多层渐变的自定义 rule 套路

UnoCSS 默认不带 `-webkit-text-stroke`（描边文字），加规则即可：

```ts
rules: [
  [/^text-stroke-(\d+)$/, ([, w]) => ({
    '-webkit-text-stroke-width': `${w}px`,
    'color': 'transparent',
  })],
  [/^text-stroke-(\[[^\]]+\]|white|black|brand)$/, ([, c]) => {
    const color = c.startsWith('[') ? c.slice(1, -1) : c === 'brand' ? 'var(--brand-500)' : c
    return { '-webkit-text-stroke-color': color }
  }],
]
```

用法：`<span text-stroke-2 text-stroke="[rgba(255,255,255,.85)]">`。

##### 「能否完全替代 scoped CSS？」诚实回答

**理论上 100% 可以，实战 92%**。剩下 8% 必须 / 推荐留 plain CSS：

| 场景 | 为什么留 CSS | 例子 |
|---|---|---|
| **`@keyframes` 本身** | UnoCSS 不能定义，只能引用 | `keyframes shimmer { ... }` |
| **Vue Transition 命名 class** | Vue 命名约定要求 plain class | `.fade-slide-enter-active` |
| **依赖 `::before` `::after` 伪元素** + 复杂结构 | utility 写多层伪元素链路啰嗦 | `.with-noise::after`（噪点 overlay）|
| **依赖 JS 注入 CSS var** | `--mx --my` 跟随鼠标场景 | `.cursor-glow` |
| **状态化 `[data-state='up']` 联动多个子元素** | 父级状态控制多个 selector 时 utility 难表达 | health card 状态色联动 |

#### 2.5.5 页面过渡 / 路由动画基线（强制）

**所有应用都必须**为路由切换提供过渡动画，避免页面"硬切"的廉价感。统一基线：

- **默认动效 = `fade-slide`**：280ms 入场（12px 上滑）+ 200ms 离场（6px 上飘）
- **节奏控制**：入场 ≤ 320ms、离场 ≤ 220ms（B 端工具不能让用户等），同时配合 `mode="out-in"` 避免新旧页同框抖动
- **滚动复位**：路由切换时 `scrollBehavior` 始终回到顶部（除非有 `meta.keepScroll`）
- **可达性**：`prefers-reduced-motion: reduce` 媒体查询下**完全禁用过渡**（不是减速，而是 0ms），与 reveal/aurora 同标准
- **个别页定制**：通过 `route.meta.transition` 字段覆盖默认（如登录页用 `fade` 不带位移，404/empty state 用 `fade-scale`）
- **嵌套路由**：带 layout 的场景，**只对内层 view 加过渡**，外层 sidebar/topnav 保持静止；用 `<RouterView v-slot="{ Component, route }">` 在内层包 `<Transition>`

##### 速度感工程学（避免「太快显跳」「太慢显粘」）

| 维度 | 反面教材 | 正确做法 |
|---|---|---|
| **时长** | 入场 180ms 显跳；入场 ≥ 400ms 显粘 | **页面入场 260-320ms 是甜区**；离场比入场短 60-80ms |
| **曲线** | hover 用的 `expo-out`（前段冲得猛）放到页面转场 | 入场用 `cubic-bezier(0.32, 0.72, 0, 1)`（iOS spring，**末段稳**）<br>离场用 `cubic-bezier(0.4, 0, 0.6, 1)`（gentle，**不抢戏**） |
| **节奏差** | opacity 和 transform 同时间结束 → 末段"半透明残影"拖尾 | **opacity 比 transform 略短**（180 vs 280），让位置先稳下来再淡入 |
| **位移幅度** | 入离场幅度一样大 | **入场幅度 > 离场幅度**（12px vs 6px），制造"前进感" |
| **属性范围** | `transition: all 200ms` | **显式列属性**，避免浏览器把 `color/border-radius/...` 全做插值 |
| **GPU** | `translateY()` / `scale()` | **`translate3d()` / `scale3d()`** 强制 GPU compositing，掉帧少 |

> **判断动画好不好的简单方法**：路由切换时眼睛能否"跟得上但又看得到"。跟不上 = 太快，等不及 = 太慢。300ms 上下做加减是黄金区间。

##### 最小落地模板（Vue 3 + Vue Router 4）

```vue
<!-- App.vue（或 layout 内层）-->
<RouterView v-slot="{ Component, route }">
  <Transition :name="(route.meta.transition as string) || 'fade-slide'" mode="out-in" appear>
    <component :is="Component" :key="route.fullPath" />
  </Transition>
</RouterView>
```

```css
/* global.css —— 默认 fade-slide */
.fade-slide-enter-active {
  transition:
    opacity   var(--dur-fade)    var(--ease-page-in),
    transform var(--dur-page-in) var(--ease-page-in);
  will-change: opacity, transform;
}
.fade-slide-leave-active {
  transition:
    opacity   160ms               var(--ease-page-out),
    transform var(--dur-page-out) var(--ease-page-out);
  will-change: opacity, transform;
}
.fade-slide-enter-from { opacity: 0; transform: translate3d(0, 12px, 0); }
.fade-slide-leave-to   { opacity: 0; transform: translate3d(0, -6px, 0); }

.fade-enter-active { transition: opacity var(--dur-page-in)  var(--ease-page-in); }
.fade-leave-active { transition: opacity var(--dur-page-out) var(--ease-page-out); }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.fade-scale-enter-active {
  transition:
    opacity   220ms var(--ease-page-in),
    transform 320ms var(--ease-bounce);
  will-change: opacity, transform;
}
.fade-scale-leave-active {
  transition:
    opacity   160ms var(--ease-page-out),
    transform 200ms var(--ease-page-out);
}
.fade-scale-enter-from { opacity: 0; transform: scale3d(0.96, 0.96, 1); }
.fade-scale-leave-to   { opacity: 0; transform: scale3d(1.03, 1.03, 1); }

@media (prefers-reduced-motion: reduce) {
  .fade-slide-enter-active, .fade-slide-leave-active,
  .fade-enter-active, .fade-leave-active,
  .fade-scale-enter-active, .fade-scale-leave-active { transition: none !important; }
  .fade-slide-enter-from, .fade-slide-leave-to,
  .fade-scale-enter-from, .fade-scale-leave-to { transform: none !important; }
}
```

**强约束**：

- `key="route.fullPath"` 必须给（而非 `route.path`），否则 `/jobs/1 → /jobs/2` 同组件不会触发过渡
- `mode="out-in"`，**不要**用 `in-out`（会出现两份 DOM 同框抖动 + 闪烁滚动条）
- 不要在 Transition 里包含 `position: fixed/sticky` 的 header — 会跟着动；header 留在 Transition **外面**
- 离场动画 `transform` 方向应与入场**相反**（入场上滑 → 离场上飘），制造"前进感"
- **禁止** `transition: all`，必须显式列出参与动画的属性
- transform 始终用 `translate3d` / `scale3d`，触发 GPU compositing，scroll 抖动率明显下降

> 💡 **进阶**：带方向感的 history 过渡（前进右进、后退左出）可在 router `beforeEach` 比对历史栈记录方向，在 `meta._transitionDirection` 上挂值给 Transition 用。

---

## Phase 3：编码实现

进入编码前，**先输出两份文档**：

1. `docs/project-dev-plan.html` — 总体开发计划（一次性输出，不频繁改）
2. `docs/project-milestones.html` — **活文档**：里程碑实时进度 + 变更日志 + 风险台账 + 关键决策 + 给未来 agent 的续接说明

> ⚡ **`project-milestones.html` 是 Phase 3 的核心交接物**。每个里程碑完成（哪怕是部分完成）都必须更新本文档；
> 这是下一个 agent / 新加入的人接手时第一份要看的文档，能节省他们 80% 的"摸索时间"。
> 模板要点：**仪表盘 + 时间线 + 续接说明 + 每个里程碑的详情卡 + 风险登记台账 + ADR-lite 决策表 + 变更日志**。

### 关于 `project-dev-plan.html`

文档必须包含：

1. **总览**
   1. 目标与产出：列出 Phase 1/2 的输入文档清单 + Phase 3 末态交付物
   2. 总体策略：纵切而非横切、契约先行、小步快跑、状态机闭环 4 条原则
   3. 里程碑甘特图：M0–M5 共 6 个里程碑，按天/周可视化
2. **里程碑**
   1. 每个里程碑的详细规划卡片（4 张卡 = 后端 + 前端 + 联调 + 风险/注意事项）
3. **开发环境准备**（进入 M0 之前必做，参考本文 3.1 章节）
   1. 工具与版本清单：JDK / Maven / Node / 包管理器 / Docker / Git / OpenSSL
   2. 本地服务方案：Docker Compose vs 本机原生，含端口冲突预案
   3. 凭据与密钥：JWT RS256 密钥生成、BCrypt hash 生成、`.env` 填值
   4. 代码仓库与 Git 规范：仓库布局、`.gitignore`、分支策略、commit 规范
   5. IDE 与编辑器推荐插件
   6. 环境自检脚本（Bash + PowerShell 双版本）
   7. 常见问题预案：端口冲突 / 镜像源 / WSL2 / BCrypt $ 转义等
4. **实施细节**
   1. 完整任务清单表格：编号 `MX-XX`、任务、类型（BE/FE/MIX/OPS/DB）、依赖、预估工时、优先级
   2. 关键技术要点代码片段：JWT 流程、状态机声明、看板回滚、文件上传校验、全文搜索
   3. 联调节点表：每个里程碑末的必跑场景 + 关键检查项
5. **协作规范**
   1. AI 辅助 SOP：Prompt 模板、生成粒度、多方案取舍原则
   2. 代码自检清单：后端 + 前端，参考本文 3.4 章节
   3. DoD（Definition of Done）**：单任务完成标准，参考本文 3.5 章节
6. **风险与交付**
   1. 风险矩阵：高/中/低 3 档 + 应对策略
   2. 阶段交付物清单

按需扩充。

### 3.1 开发环境准备（M0 之前必做）

> ⚠ **超过一半的 MVP 项目卡在环境问题**：JDK 版本不匹配、端口被占、镜像拉不下、JWT 密钥未生成……进入 M0 之前花 1–2 小时把环境彻底打通，能节省后面 3–5 倍调试时间。本节自检 100% 通过才能开始编码。

**工具与最低版本**：

| 工具 | 最低版本 | 用途 | 检查命令 |
|------|---------|------|---------|
| JDK | 21 (LTS) | 后端运行/编译 | `java -version` |
| Maven | 3.9+ | 后端构建 | `mvn -v` |
| Node.js | 20 (LTS) | 前端工具链 | `node -v` |
| 包管理器 | bun 1.3+ / pnpm 9 / npm 10 | 前端依赖 | `bun -v` |
| 容器引擎 | Docker 24+ **或** Podman 4+ | 本地 pg/redis | 见下 |
| compose | Docker Compose v2 **或** docker-compose v1 **或** podman compose | 编排 | 见下 |
| Git | 2.40+ | 版本控制 | `git --version` |
| OpenSSL | 3.0+ | 生成 JWT 密钥 | `openssl version` |

| 引擎 | 启动命令 | 备注 |
|------|---------|------|
| Docker + Docker Compose | `docker compose -f xxx up -d` | 现代默认 |
| `docker-compose` v1（独立 binary） | `docker-compose -f xxx up -d` | 兼容旧仓 / Podman 上常装 |

**本地服务方案**：

- **方案 A（MVP 推荐）**：compose 起 `postgres` + `redis`，schema.sql 挂到 `/docker-entrypoint-initdb.d/` 自动初始化
- **方案 B（长期开发）**：本机原生安装；启动更快但跨平台版本对齐困难
- 默认端口 5432 / 6379 / 8080 / 5173 被占时在 compose 改映射如 `15432:5432`

**凭据与密钥（不可省略）**：

```bash
openssl genrsa -out jwt-private.pem 2048
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
htpasswd -bnBC 12 "" "YourPassword" | tr -d ':\n'
```

`.env`、`*.pem`、`*.b64`、`secrets/` 必须全部加入 `.gitignore`，**任何 commit / 截图 / 日志都不得包含真实密钥**。

**Git 规范**：

- 仓库布局：MVP 阶段用 monorepo（`xxx-backend/` + `xxx-frontend/` + `docker-compose.yml`）
- 分支：`main` 保护 + 每里程碑一条 `feature/m1-auth`
- commit 格式：`[模块] 动词 + 内容`，禁止 `WIP` / `update` / `fix bug`
- `.gitignore` 必含：`.env*` / `*.pem` / `*.b64` / `target/` / `node_modules/` / `dist/` / `logs/` / `uploads/` / `.idea/` / `.DS_Store`

**环境自检**：提供 `scripts/check-env.sh`（Bash）和 `scripts/check-env.ps1`（PowerShell），覆盖：

1. JDK / Maven / Node / Docker / Git / OpenSSL 版本
2. 5432 / 6379 / 8080 / 5173 端口空闲
3. `docker compose up -d` 后 pg / redis 健康
4. `psql ... -c "\dt"` 能列出全部表
5. `redis-cli ping` 返回 PONG
6. JWT 密钥已生成并写入 `.env`

**常见问题预案**：

| 问题 | 解决 |
|------|------|
| 端口被占 | compose 改映射；或停掉本机服务 |
| Maven 慢 | `~/.m2/settings.xml` 配阿里云 mirror |
| npm/bun 慢 | `npm config set registry https://registry.npmmirror.com` 或 `~/.bunfig.toml` 配 `registry` |
| 容器拉镜像超时 | Docker Desktop → Engine 配国内 mirror；Podman 改 `~/.config/containers/registries.conf` 加 mirror |
| BCrypt hash 含 `$` 被 shell 截断 | 单引号包裹或写到 application.yml |
| PowerShell 多行命令 | 用反引号 \` 续行，**不要**用反斜杠 \\ |

按需扩充。

### 3.2 参考里程碑划分

| 里程碑 | 名称        | 工期参考 | 关键产出 |
|--------|-------------|----------|----------|
| M0     | 项目基建    | 1 天     | 前后端骨架 + dev compose + /health 跑通 |
| M1     | 认证模块    | 2 天     | 注册/登录/Refresh + JWT 拦截器 + 路由守卫 |
| M2     | 核心 CRUD   | 1 天     | 主实体增删改查 + 列表/详情页 |
| M3 ⭐  | 项目亮点功能 | 2 天     | 以具体项目为准 |
| M4     | 辅助模块    | 2 天     | 文件上传、子实体 CRUD、数据看板 |
| M5     | 打磨交付    | 2 天     | UI 打磨 + 完整 docker-compose + README + 演示 |

按需扩充。

每个里程碑结束都必须**能跑起来、能演示一个完整场景**。Mn 未通过联调验收禁止进入 Mn+1。

### 3.3 AI 辅助 SOP

**契约先行**：每次让 AI 写接口前，必须把以下信息一起喂给它：
- 任务（接口路径 + 一句话目标）
- 输入约定（DTO 字段 + 校验规则）
- 输出约定（VO 字段 + ApiResponse 包装）
- 业务规则（编号列举，含权限、状态机、唯一约束）
- 参考代码风格（贴一段已有 Service 30 行）
- 数据库 DDL 相关表的 CREATE TABLE

**生成粒度**：
- ✅ 一次生成「Controller + Service + Mapper」三件套（同一业务）
- ✅ 一次生成「一个 Vue 页面 + 对应 api 函数」
- ❌ 避免一次生成整个模块（如全部认证）—— 难以审阅
- ❌ 避免攒到 30 个文件再统一调试 —— 累积错误难定位

**多方案抉择**：
- 选最接近已有代码风格的那一种，不要混用（已用 MyBatis-Plus 就不再引 JPA）
- 方案差异显著时让 AI 列 trade-off，自己定夺，**不要直接接受 "AI 推荐"**
- 与 `project-tech-design.html` 冲突时，**以设计文档为准**

### 3.4 代码自检清单（每次 AI 生成后必做）

**后端 7 项**：

1. 字段名/类型是否与 DDL 对齐？枚举序列化用 name 而非 ordinal
2. 接口是否加 `@PreAuthorize` 或在 SecurityConfig 配规则？
3. 多表写入是否 `@Transactional`？状态变更是否与审计日志同事务？
4. 参数校验注解（`@Valid` + DTO 的 `@NotNull/@Size/...`）是否齐全？
5. SQL 是否用 `#{}` 占位符或 Wrapper，**禁止字符串拼接**
6. 状态机相关代码是否调用了统一校验方法，未自定义 if 判断？
7. 日志里有无敏感字段（密码、token、手机号、身份证）？

**前端 7 项**：

1. 类型是否从 `types/api.d.ts` 引入而非现写？
2. 异步操作是否带 loading？错误是否 toast？
3. 表单提交前是否 `formRef.validate()`？
4. 路由是否在 `meta.roles` 声明角色？
5. 是否有 hardcode 颜色（应使用 Naive UI theme token）？
6. 接口失败时本地乐观更新是否有回滚？
7. `any` 是否已消除？枚举是否从 `enums.ts` 引用？

### 3.5 单任务完成判定（DoD）

1. 本地能跑通对应场景（含至少一条 happy path + 一条异常 path）
2. 接口返回结构符合 `{ code, msg, data }`，HTTP 状态码与 code 一致
3. `npm run lint` / `mvn verify` 无新增报错
4. 关键逻辑（状态机、文件上传校验等）至少 1 个单测覆盖
5. 提交前 `git diff` self-review，删除 console.log / 调试代码
6. commit message 遵循 `[模块] 动词 + 内容`（如 `[auth] add refresh endpoint`）

### 3.6 高风险点速查（务必单测覆盖）

- **状态机非法流转**：用 `Map<Stage, Set<Stage>>` 声明合法图 + 终态不在 key 中保护
- **Token refresh 死循环**：拦截器内 refresh 失败必须清状态跳 `/login`，并用队列防重入
- **看板并发拖拽**：后端基于 `from_stage` 校验当前状态，UI 失败时 snapshot 回滚
- **文件上传越权**：所有下载接口必须查 application 归属，**禁止** nginx 直接静态暴露 `/uploads/`
- **AI 字段名漂移**：契约先行 + 自检清单第 1 项双重防线

---

## Phase 4：交付

### 本地运行

提供 `docker-compose.yml`，一条命令启动所有服务：

```bash
# Docker
docker compose up -d
# 或 v1 / Podman 兼容写法
docker-compose up -d
# Podman 原生
podman compose up -d
```

包含服务：postgres、redis、backend、frontend（nginx）

---

## 参考文档

| 类型 | 路径 | 性质 |
|---|---|---|
| 需求文档 | `docs/project-requirements.html` | 静态 · Phase 1 末完成 |
| 数据库 Schema | `docs/project-schema.sql` | 单一来源 · 改动需 dev:reset |
| 技术设计文档 | `docs/project-tech-design.html` | 静态 · Phase 2 末完成 |
| Phase 3 总体计划 | `docs/project-dev-plan.html` | 半静态 · 大改才动 |
| **里程碑活文档** | `docs/project-milestones.html` | **活文档 · 每个里程碑必更** |

> 接手项目第一份要看：**`docs/project-milestones.html` 的「续接说明」**章节。

---
