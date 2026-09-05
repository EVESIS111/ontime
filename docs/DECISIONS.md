# DECISIONS.md

| 日期 | 决定 | 理由 | 参考 |
|---|---|---|---|
| 2026-09-05 | baseline 固化于 tag baseline/v10.3-stable,真机基线 13ms+slide-ack | 重构可回滚 | 任务书 §22 |
| 2026-09-05 | 工作树中 .gradle/app/build 的删除被提交(非丢弃用户内容,系上轮清理的完成)+新增 .gitignore | 让 vcs 不含构建产物 | git 惯例 |
| 2026-09-05 | ScheduleEngine 判定"不重构" | 1000 组异构对拍=行为规格,稳定优先 | 任务书 §2 |
| 2026-09-05 | Hilt 延迟决定至 PHASE4 实测装配复杂度 | 项目极小,先引 Compose 边界 | 任务书 §25 |
| 2026-09-05 | 保留 KeepAliveService 直到 PHASE10 A/B 实测 | 华为环境经验性依赖,无实测不删 | 任务书 §34 |
