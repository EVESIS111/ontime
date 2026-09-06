# MIGRATION_PLAN.md(对齐总任务书 PHASE0-12)

- [x] PHASE0 审计(PROJECT_AUDIT.md,真机基线 13ms+slide-ack ✅)
- [x] PHASE1 CLAUDE.md+docs
- [x] PHASE2 baseline(tag baseline/v10.3-stable;APK/DB/触发证据已存档)
- [x] PHASE3 构建标准化:补 gradle wrapper(./gradlew 可用),仍锁 Gradle 8.5;单独提交单独验证
- [x] PHASE4 架构外壳(工具链先行,一次一个变量,每步真机 60s 回归):
  - 4a 工具链升级(已完成:真机 15ms 回归 ✅):Gradle 8.5→9.4.1 / AGP 8.2.2→9.2.1 / Kotlin 1.9.22→2.3.21 / compileSdk 33→36(SDK 平台 36 本机已装;targetSdk 33 不动,运行行为零变化)。零新依赖,现有 926 行旧代码必须原样编译+真机回归
  - 4b Version Catalog ✅(对齐官方模板 libs.versions.toml,版本基准=2026-09-05 实拉 architecture-templates:base)
  - 4c Compose 基础设施 ✅(真机像素壳验证)(BOM+M3+activity-compose+lifecycle)+OnTime Theme 骨架+MainActivity setContent 壳;**AlertActivity 暂留 View 体系保 5-id 契约**(alertTitle/alertMessage/slideTrack/btnSlide/btnSnooze),PHASE6 样张时再迁
  - 4d Repository/ViewModel 边界 ✅(开关 4→3→4 双向验证)(包现有 Db,不动表结构与 ScheduleEngine)
  - 版本依据:AGP 9.2 官方 release notes(Gradle≥9.4.1/JDK17,本机 Temurin 17.0.20 ✓);JDK 不动
- [x] PHASE5 OnTime Pixel DesignSystem(字体子集 659KB+Token+组件,清晰度 zoom 验收)(Kenney CC0 基底+蓝金语言;建法参照 Backpack/Komposto;组件=成熟交互换皮,先有使用场景再抽象)
- [x] PHASE6 Home+Alert(Compose 全量:Home 像素版+Alert 迁移+slide-ack 15ms 真机闭环)(开工前各页 Reference Board 落 UI_REFERENCE_MAP.md)→真机截图→用户过目(不可跳过)
- [x] PHASE7 全量 Compose UI(Editor 四类型表单+Settings+NavHost;UI 建提醒 E2E 触发+免打扰规则验证)(编辑/设置/交互/动效;Window Size Class 适配,禁硬编码 818dp;核心组件 Preview+截图回归)
- [x] PHASE8 Room 迁移(v3→v4 重建表,真机无损 17ms;fresh+upgrade+自定义数据三路径验证)(schema 导出→Migration→双跑比对→真机验证→才删旧 Db;禁 destructive)
- [x] PHASE9 Alarm 审计(唯一缺口 PACKAGE_REPLACED 已修并实测;见 ALARM_AUDIT.md)(vs DeskClock/Brutus/reReminder 比较表,只改明确缺陷)
- [x] PHASE10 可靠性(Alarm Health+权限矩阵+13 项真机矩阵;Doze 长时挂机与划掉列入持续项,见 RELIABILITY_MATRIX.md)(§42 测试矩阵:Doze/重启/时区/划掉/权限开关)
- [ ] PHASE11 全量回归
- [ ] PHASE12 release+归档
