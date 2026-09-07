# DEVICE_DBY2-W00.md — 华为平板实测要点

- adb=~/Library/Android/sdk/platform-tools/adb;设备常掉线:kill-server/start-server 重试
- **30 秒无操作自动灭屏**→灭屏时触发走全屏通知(弹窗不在前台,非 bug);60s 等待期每 14s input keyevent KEYCODE_WAKEUP 保屏
- 设备常横放:测竖屏先 settings put system accelerometer_rotation 0;user_rotation 0,测完恢复 1
- 截图前必 KEYCODE_WAKEUP+wm dismiss-keyguard(否则全黑);Android12 启动闪屏期截图也黑,冷启动等 8-10s
- uiautomator dump 常因动画无 idle;用 dumpsys activity top | grep "app:id/xxx"(bounds 为相对父坐标)或截图像素定位
- release 不可调试；DB 运维以 DB_MAINTENANCE.md 为准：禁止 uninstall；装同签名 debug 后用 tools/backup-device-db.sh、tools/restore-device-db.sh，结束装回 release 并核实闹钟。run-as 内不能 && 串联
- 历史 60s 验收 SQL（仅参考；优先 UI 创建本轮测试事项，SQL 必须先备份且只改本地工作副本）:
  INSERT INTO reminders(...列...) VALUES('基线测试','该喝水了,起来活动一下,补充水分。',NULL,'daji','coin','ONCE',0,0,60,<now+58s ms>,5,1,0,0,-1,-1)
  推回→装 release→启动→保屏 60s→fire_log 应<100ms+slide-ack→清测试行
- 归档:/sdcard/OnTime项目/(v10/OnTime.apk + 用户资产四项,资产只读)

- v12 用户安排：设备另约，本地阶段不操作平板。熄屏测试至少一小时，不唤醒、不保屏，记录真实 idle 状态；保屏技巧仅适用于前台交互验收。
