# ALARM_AUDIT.md — Alarm 架构审计(2026-09-06,PHASE9)

> 对照:Android 官方文档 → AOSP DeskClock(模式) → Brutus(MIT,源码已研)。
> 原则:找缺失,不重写。现实测延迟 13-36ms,行为规格已由真实用户环境验证。

| 维度 | OnTime 现状 | AOSP/Brutus 对照 | 判定 |
|---|---|---|---|
| 注册 API | `setAlarmClock`+`AlarmClockInfo`(showIntent 主界面) | 官方推荐闹钟型路径;Brutus 同 | ✅ 一致 |
| Exact 权限 | SCHEDULE_EXACT_ALARM + 三级降级链 + Health UI + 恢复广播重排 | AOSP 时钟免权限;Brutus 有 permission check | ✅ 超越(含华为 AppOps 撤销防御) |
| PendingIntent | FLAG_UPDATE_CURRENT + FLAG_IMMUTABLE | AOSP 同 | ✅ |
| Boot 恢复 | BOOT_COMPLETED→scheduleAll+KeepAlive | AOSP/Brutus 同 | ✅ |
| TIME/TIMEZONE | TIME_SET + TIMEZONE_CHANGED → 重排 | AOSP 有;**Brutus 部分版本缺** | ✅ |
| **App 更新自愈** | ~~缺失~~ → **本次补齐 MY_PACKAGE_REPLACED**(实测:覆盖安装清闹钟,旧版需手动启动) | AOSP/Brutus 均有 | 🔧 本次修复 |
| 权限恢复 | SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED → 重排 | 官方建议(本次落地) | ✅ |
| 触发链 | Receiver:查库→免打扰顺延→logFire→高优无声通知+fullScreenIntent→尽力 startActivity→自续 | AOSP 走 FGS+通知;Brutus 类似 | ✅ 语义一致 |
| 免打扰 | 00:00-08:30 顺延 setExactAndAllowWhileIdle | 产品规则(引擎行为规格) | ✅ 保留 |
| 音频 | USAGE_ALARM 全链(音效→音乐→pack→TTS);播放窗口=AlertActivity 2min(KEEP_SCREEN_ON) | AOSP 播放全程持 partial WakeLock | ⚠️ 差异可接受(语音<30s<120s 窗口);若未来长音频再加 WakeLock |
| Snooze | setExactAndAllowWhileIdle + SecurityException 降级 | AOSP 同 API | ✅ |
| 自续 | Receiver 尾 scheduleNext(INTERVAL 无损链) | — | ✅(今晨 45min 五连发实证) |
| KeepAlive | FGS dataSync + 常驻通知 + onStartCommand 自愈 | OEM 对抗经验件 | ✅ 保留至 PHASE10 A/B |

## 结论
唯一实质缺口(覆盖安装不自愈)本次修复;其余与官方/AOSP 路径一致或更防御。
WakeLock 差异记录为已知取舍,不视为缺陷。
