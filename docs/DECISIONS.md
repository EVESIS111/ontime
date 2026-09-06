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
| 2026-09-06 | Exact Alarm 权限定案:保持 SCHEDULE_EXACT_ALARM(不换 USE_EXACT_ALARM),+ ACTION_EXACT_ALARM_PERMISSION_STATE_CHANGED 恢复监听 + ACTION_REQUEST_SCHEDULE_EXACT_ALARM 引导 + canScheduleExactAlarms 健康检查;降级链保留为 safety net | USE 自动授予但仅限闹钟/日历核心应用,Google Play 政策一般 reminder App 不符;SCHEDULE 用户/系统可撤销但长期合规;华为实测为 AppOps 型可静默撤销(09-05 崩溃两次) | developer.android.com/develop/background-work/services/alarms + Android14 behavior change 页 + Play 政策讨论(2026-09-06 查证) |
| 2026-09-06 | Room 迁移:Reminder 原地升级 Entity(列映射含默认值),Db 门面签名不变内部换 Room,version 3→4 + MIGRATION_3_4 重建表(根因:旧库 PK 无显式 NOT NULL,pragma notnull=0 而 Room 要求 1 → "Pre-packaged invalid schema");allowMainThreadQueries 暂留(数据毫秒级,KMP 化时统一 suspend) | 渐进迁移零调用点改动;官方 Room 2.8.4 stable(KMP-ready);schema 导出 app/schemas | 官方 Room 文档 + 实测 diff 定位 |
| 2026-09-06 | 语音包恢复事故:uninstall 清 filesDir 抹掉用户 94 个角色语音(daji/diaochan/xiaoqiao/zhaojun/yunyang);从 /sdcard/OnTime项目/语音包产物(只读资产)经 App 自身 importFromDownload 恢复(94/94,pack:daji 命中 17ms 验证);**run-as 直推 filesDir 的文件 App 进程不可见(华为数据视图隔离),语音/资产必须走 App 进程内导入**;补 READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE 权限(导入功能在 targetSdk 33 本缺权限);Mac 侧双备份 backups/voicepacks | 资产纪律:uninstall 前必须备份 filesDir;run-as 不可用于 filesDir 资产投放 | 实测(09-06 13:0x) |
