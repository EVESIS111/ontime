# BASELINE_V11.md — v11 Initial Acceptance 冻结基线(2026-09-06)

> 历史记录，仅描述标题版本，不代表当前源码或 v12 验收；当前状态见 docs/EXECUTION_STATE.md。

> 后续所有 Stable Foundation 改造的回退点。git tag: `v11-initial-acceptance`

## 构建
- Git commit: b8f53dc(文档)/1a09728(代码)
- Release APK: 4,012,542 bytes;SHA256 前缀 `6eaef5b0a4873927…`
- 归档:/sdcard/OnTime项目/v11/OnTime-v11-initial-acceptance.apk
- versionCode=3 / versionName=1.2.0(定版在 PHASE12)
- 工具链(本阶段冻结):Gradle 9.4.1 / AGP 9.2.1(内置 Kotlin)/ compileSdk 36 / targetSdk 33 / Compose BOM 2026.05.01 / navigation 2.9.8

## 数据库(Schema v3,SQLiteOpenHelper)
- 4 条用户 Reminder:喝水(daji/INTERVAL 45min 窗口 09:00-21:30)、学英语(zhaojun/DAILY 13:00)、下班(xiaoqiao/WEEKLY mask62 21:30)、科目四(TTS Yunyang/DAILY 11:30);全部 enabled
- fire_log 16 条(含 DIAG 2 条遗留)
- journal mode=delete

## 真实可靠性证据(2026-09-06 晨,用户环境无人值守,夜间构建版)
| 时刻 | 提醒 | 延迟 | 状态 |
|---|---|---|---|
| 09:00 | 喝水 | 36ms | screen=off,ui=shown |
| 09:45 | 喝水 | 19ms | screen=off,ui=shown |
| 10:30 | 喝水 | 20ms | screen=off,ui=shown |
| 11:15 | 喝水 | 18ms | screen=off,ui=shown |
| 11:30 | 科目四 | 22ms | screen=on,ui=shown |
| 12:00 | 喝水 | 31ms | screen=off,ui=shown |

INTERVAL 45min 周期全天无丢失;灭屏触发正常;弹窗 2min timeout 正常(用户未确认时的兜底)。

## 已知风险(本阶段处理)
1. 华为 SCHEDULE_EXACT_ALARM 为 AppOps 型,覆盖安装/重置可撤销 → 已有三级降级,待 Health System
2. DB 运维依赖人工 adb 序列 → 待 safety scripts
3. 手写 SQLite schema 曾出 fresh-install 缺列 → 待 Room 迁移
4. 一次"enabled 集体归零"未归因(已恢复未复现)

## 回退方式
`git checkout v11-initial-acceptance` + `./gradlew assembleRelease`;设备侧 DB 不受代码回退影响。
