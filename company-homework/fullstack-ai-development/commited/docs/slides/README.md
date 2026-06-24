# Slidev 演示稿

用 [Slidev](https://sli.dev) 承载过程性文档的汇报演示。

## 运行

```bash
cd docs/slides
# 任选其一
npx @slidev/cli overview.md          # 临时运行
# 或安装后运行
npm i && npm run dev                  # http://localhost:3030
```

## 导出

```bash
npx @slidev/cli export overview.md --format pdf   # 导出 PDF
npx @slidev/cli build  overview.md                # 导出静态站点到 dist/
```

## 文件

| 文件 | 内容 |
| --- | --- |
| `overview.md` | 项目总览汇报：背景 → 范围 → 架构 → 数据 → 计划 → 演示要点 |
| `demo.md` | **全流程手动测试 / 演示脚本**：点到哪、看什么、预期什么（端到端闭环验收） |

运行演示脚本：`npx @slidev/cli demo.md`（或 `npm run dev` 后访问对应文件）。
