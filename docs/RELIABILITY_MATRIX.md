# RELIABILITY_MATRIX.md — 真机测试矩阵(2026-09-06,持续更新)

> 记录规范:expected/actual/latency/notes;真实 AlarmManager 到点,非人工触发。
> 设备 DBY2-W00(HarmonyOS 4.2 / Android 12 基座);SOP 见 DEVICE_DBY2-W00.md。

## 已完成 ✅

| # | 场景 | Expected | Actual | 判定 |
|---|---|---|---|---|
| 1 | 前台 App,60s ONCE 触发(Compose Alert) | <100ms,音效,弹窗,slide-ack | 15ms,coin,ui=shown,slide-ack | ✅ |
| 2 | 灭屏状态自然触发(用户真实晨间) | 按时响,弹窗 | 09:00-12:00 喝水×5 连发,18-36ms,screen=off,ui=shown | ✅ |
| 3 | INTERVAL 45min 全天链(用户环境) | 周期无丢失 | 09:00/09:45/10:30/11:15/12:00 全中 | ✅ |
| 4 | 免打扰时段触发(00:11) | 顺延 08:30,不响 | quiet-deferred→08:30 ×3,未打扰 | ✅ |
| 5 | Room 管道触发 | 数据层迁移后行为不变 | 17-20ms,pack:daji 恢复后命中 | ✅ |
| 6 | 权限撤销(Exact Alarm) | 不崩,降级,可感知 | deny→fallback-inexact 留痕+Home 警示,无 crash | ✅ |
| 7 | 权限恢复 | 自动重排 exact | allow 后 4 闹钟全量 exact 恢复 | ✅ |
| 8 | 覆盖安装(install -r) | MY_PACKAGE_REPLACED 自愈 | 装包后不启动,闹钟自动 4 个 | ✅ |
| 9 | 开关提醒(Compose UI) | 闹钟增删+DB 一致 | 4→3→4,next_fire_at 正确重算 | ✅ |
| 10 | UI 全流程建提醒(Editor→保存→触发) | 真实调度 | E2E 每 1 分钟行建后即触发 | ✅ |
| 11 | 语音包缺失 | TTS 回退不崩 | miss→tts 正常 | ✅ |
| 12 | fresh install(Room v4) | 建库+种子含 sound_id | 4 种子正确(sound_id 历史 bug 已随 Room 修复) | ✅ |
| 13 | v3→v4 迁移(真实用户库) | 数据无损 | 4 提醒 16 字段+24 fire_log 逐字段一致 | ✅ |

## 待补(设备重启后 adb 未回连,恢复后执行)
| # | 场景 | 方法 |
|---|---|---|
| 14 | **设备重启 Boot 恢复** | reboot 后不启动,数闹钟+服务(BootReceiver 代码与 install -r 同路径,风险低) |
| 15 | 划掉 App(recents) | APP_SWITCH+swipe 后等触发 |
| 16 | Doze 深度(熄屏 1h+) | 长时挂机(低优先,华为有 KeepAlive) |
| 17 | 时区/时间修改 | TIME_SET/TIMEZONE_CHANGED 已注册,行为同 Boot 路径 |

## 已知非缺陷
- 通知横幅(灭屏触发、弹窗不在前台):SOP 记载的华为行为,fullScreenIntent 通知为设计兜底
- WakeLock 不持有:播放窗口=AlertActivity 2min(语音<30s),审计记录取舍
