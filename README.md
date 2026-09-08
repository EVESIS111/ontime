# 「准时」OnTime

到点由游戏角色语音(妲己 / 王昭君 / 小乔 / 云扬)播报的**本地提醒 Android App**——无网络、无账号、无云端,数据全在本机。视觉方向:**Instant Pixel UI**(像素游戏机质感 × 工具软件速度,零动画预算,状态直切)。

- 唯一验收设备:华为平板 DBY2-W00(HarmonyOS 4.2 / Android 12 基座,1840×2800)
- 包名:`dev.evesis.ontime`,单 App 模块

## 当前状态(2026-09-08)

**v12 前端大改版进行中,未正式定版。**

- 方向:四页统一为 **pixel console(游戏机)风格**——展示页做成游戏机样式,边缘与交互换成像素掌机 UI(main@`2296afc`)
- 已完成:v12 运行缺陷修复(编辑页分组/保存固定底部/删除确认/触控约束)、WAL 数据库恢复脚本配对修复、版本定版 12.0
- 界面样张:`artifacts/ui-review/`(改造前后对照 + 审计放大图)
- 待办:真机回归、≥1 小时熄屏长测、24 小时真人试用 → 通过后打 `v12.0-final` 并归档

版本线:v10.3 后端基线(tag `baseline/v10.3-stable`)→ v11 Compose 像素四页 + 微信式左滑删除(`150023f`)→ **v12 游戏机风格重构(进行中)**

## 文档入口(按序读)

1. [`CLAUDE.md`](CLAUDE.md) — 项目最高规范(必读)
2. [`docs/EXECUTION_STATE.md`](docs/EXECUTION_STATE.md) — 当前状态与下一步
3. `docs/PROJECT_AUDIT.md` / `docs/BASELINE_V11.md` / `docs/LAYOUT_INVARIANTS.md` / `docs/PIXEL_STYLE_REFERENCE.md` / `docs/UI_BUG_INVENTORY.md`
4. `docs/DEVICE_DBY2-W00.md` — 真机验收 SOP;`docs/DB_MAINTENANCE.md` — 数据库运维铁律

## 构建

```bash
JAVA_HOME=~/jdk/Contents/Home ./gradlew assembleDebug     # 调试包
JAVA_HOME=~/jdk/Contents/Home ./gradlew assembleRelease   # 候选包(debug 签名)
```

## 技术栈

Kotlin · Jetpack Compose · Room(v4)· AlarmManager `setAlarmClock` 三级降级 · 单元测试:ScheduleEngine 1000 组随机对拍 + DB 工具 10 项

## 红线(接手必读)

冻结核心禁止重写:ScheduleEngine / Alarm 链 / Room schema / AlertPlayer / VoicePacks(sha1 绑定)/ fire_log;`/Users/evesis/Downloads/提醒APP/` 与设备 `/sdcard/OnTime项目/` 用户资产只读;禁止 `adb uninstall`(会清空语音包);DB 运维必须走 `tools/backup-device-db.sh` / `tools/restore-device-db.sh`。
