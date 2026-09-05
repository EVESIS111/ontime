# MIGRATION_PLAN.md(对齐总任务书 PHASE0-12)

- [x] PHASE0 审计(PROJECT_AUDIT.md,真机基线 13ms+slide-ack ✅)
- [x] PHASE1 CLAUDE.md+docs
- [x] PHASE2 baseline(tag baseline/v10.3-stable;APK/DB/触发证据已存档)
- [x] PHASE3 构建标准化:补 gradle wrapper(./gradlew 可用),仍锁 Gradle 8.5;单独提交单独验证
- [ ] PHASE4 架构外壳:引 Compose/M3/VM/Nav/Repository(官方模板基准),旧核心不动
- [ ] PHASE5 OnTime Pixel DesignSystem(Kenney CC0 基底+蓝金语言)
- [ ] PHASE6 Home+Alert 静态样张→真机截图→用户过目(不可跳过)
- [ ] PHASE7 全量 Compose UI(编辑/设置/交互/动效)
- [ ] PHASE8 Room 迁移(schema 导出→Migration→双跑比对→真机验证→才删旧 Db;禁 destructive)
- [ ] PHASE9 Alarm 架构审计(vs DeskClock/Brutus/reReminder 比较表,只改明确缺陷)
- [ ] PHASE10 可靠性加固(§42 测试矩阵:Doze/重启/时区/划掉/权限开关)
- [ ] PHASE11 全量回归
- [ ] PHASE12 release+归档
