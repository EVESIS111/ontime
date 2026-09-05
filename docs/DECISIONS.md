# DECISIONS.md

| 日期 | 决定 | 理由 | 参考 |
|---|---|---|---|
| 2026-09-05 | baseline 固化于 tag baseline/v10.3-stable,真机基线 13ms+slide-ack | 重构可回滚 | 任务书 §22 |
| 2026-09-05 | 工作树中 .gradle/app/build 的删除被提交(非丢弃用户内容,系上轮清理的完成)+新增 .gitignore | 让 vcs 不含构建产物 | git 惯例 |
| 2026-09-05 | ScheduleEngine 判定"不重构" | 1000 组异构对拍=行为规格,稳定优先 | 任务书 §2 |
| 2026-09-05 | Hilt 延迟决定至 PHASE4 实测装配复杂度 | 项目极小,先引 Compose 边界 | 任务书 §25 |
| 2026-09-05 | 保留 KeepAliveService 直到 PHASE10 A/B 实测 | 华为环境经验性依赖,无实测不删 | 任务书 §34 |
| 2026-09-05 | 前端 Reference-First 补充指令固化为最高规范:复用等级 L1库→L2移植→L3换皮→L4自研、Pixel Visual≠Interaction、Window Size Class、每页 Reference Board、UI_REFERENCE_MAP.md | 用户最高指令;评价标准=减少了多少不必要新代码 | 补充指令全文 |
| 2026-09-05 | 参考池许可证全部 GitHub API 实测:brutus=MIT、yassineAbou/Clock=Apache-2.0、dose=MIT、adaptive-apps-samples/compose-samples/backpack/komposto/architecture-templates=Apache-2.0、reReminder/FossifyClock=GPL-3.0(只读) | 任务书要求逐仓核实,不凭记忆/不凭任务书声称 | GitHub repos/*/license |
| 2026-09-05 | PHASE4 走"工具链先行"路线(4a Gradle/AGP/Kotlin/compileSdk → 4b catalog → 4c Compose → 4d Repo/VM),AlertActivity 延后至 PHASE6 迁移 | 一次一个变量;避免旧 BOM 两段迁移;5-id 契约与提醒链路零风险 | AGP9.2 release notes;architecture-templates:base toml(2026-09-05 实拉) |
