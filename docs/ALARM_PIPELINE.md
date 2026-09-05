# ALARM_PIPELINE.md — 当前提醒链路(实测基线)

## 注册
MainActivity 启动链:Channels.ensure→VoicePacks.installBundled→Sounds.ensureInstalled→Db.seedDefaultsIfEmpty→Alarms.scheduleAll→KeepAliveService.start
- scheduleNext(r):!enabled→cancel;next=ScheduleEngine.next(rule,now);null(ONCE 过期)→cancel+自动停用;setAlarmClock(AlarmClockInfo(next, MainActivity pending))+fireIntent(r.id);写 next_fire_at;刷新常驻通知

## 触发
AlarmReceiver.onReceive(id):
1. find(id) 空则弃
2. 免打扰(00:00-08:30):setExactAndAllowWhileIdle 顺延至 08:30,log "quiet-deferred",return
3. pickMessage("|"分隔随机变体)→logFire(planned/actual/latency/audio 标签:音效id+pack:音色|miss:音色|tts|music)
4. 高优先级通知(无声,USE_FULL_SCREEN_INTENT)+fullScreenIntent(AlertActivity id/log/msg)
5. 尽力 startActivity(AlertActivity)
6. ONCE→setEnabled(false);立即 scheduleNext 自续

## 弹窗
AlertActivity(纯代码壳 118 行):showWhenLocked+turnScreenOn+KEEP_SCREEN_ON;标题/台词/滑动条(≥90% 确认→slide-ack→finish)/稍后(Alarms.snooze=setExactAndAllowWhileIdle now+N→snoozed)/2 分钟 timeout;播报 AlertPlayer.play:音效→audioUri 音乐→语音包(sha1)→TTS 三级

## 自愈
BootReceiver(BOOT_COMPLETED/TIME_SET/TIMEZONE_CHANGED)→scheduleAll+KeepAlive.start
KeepAliveService(前台 dataSync,无 WakeLock):onStartCommand 顺手 scheduleAll;常驻通知显示下次提醒

## 设备实测基线(2026-09-05)
60s ONCE 真实触发:latency 13ms,audio=coin+pack:daji,slide-ack 全链 ✅
