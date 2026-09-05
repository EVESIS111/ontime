# PROJECT_AUDIT.md — 2026-09-05 真实审计(v10.3 基线)

> 全部数据来自真实文件/DB/真机,不采信任何旧描述。

## 1. 真实文件结构
```
~/Desktop/准时App项目/
├─ app/src/main/java/dev/evesis/ontime/   9 个 Kotlin 文件,共 926 行
│   ScheduleEngine(105) Alarms(146) Receivers(97) AlertPlayer(118)
│   Db(193) Sounds(65) VoicePacks(87) MainActivity(32) AlertActivity(118)
├─ app/src/main/assets/   sounds(21 个音效) voices(3 内置音色×4-5 句 mp3) licences(OFL)
├─ app/src/main/res/      仅 mipmap 图标 + values(styles/strings/ids)
├─ app/src/test/          ScheduleEngineTest.kt(241 行)
├─ app/src/main/res/layout/  不存在(壳为纯代码 UI)
└─ Gradle: settings/build.gradle.kts,无 wrapper jar(见 §5)
```

## 2. 构建配置(实测)
- compileSdk=33,targetSdk=33,minSdk=29,versionCode=3,versionName=1.2.0
- Kotlin android 插件;**无 Version Catalog、无 Compose、无 AndroidX、零第三方依赖**(仅 junit 4.13.2 testImplementation)
- R8+shrinkResources 开启;release 签名=**debug 签名**(signingConfigs.debug)
- release APK **1.15MB**;JDK17(~/jdk/Contents/Home)
- 仓库:aliyun 镜像+google+mavenCentral

## 3. Manifest 组件与权限
- Activity:MainActivity(launcher)、AlertActivity(singleTask/showWhenLocked/turnScreenOn/excludeFromRecents)
- Service:KeepAliveService(foregroundServiceType=dataSync)
- Receiver:AlarmReceiver(exported=false)、BootReceiver(BOOT_COMPLETED/TIME_SET/TIMEZONE_CHANGED)
- 权限 8 项:SCHEDULE_EXACT_ALARM、POST_NOTIFICATIONS、FOREGROUND_SERVICE(+DATA_SYNC)、RECEIVE_BOOT_COMPLETED、USE_FULL_SCREEN_INTENT、REQUEST_IGNORE_BATTERY_OPTIMIZATIONS、VIBRATE

## 4. 数据库(真机实读)
- SQLite `ontime.db` 版本 3,表 reminders(16 列)+fire_log(8 列,上限 1000 条)
- 现役 4 任务(与旧任务书一致):喝水(daji/INTERVAL 45min 09:00-21:30)、学英语(zhaojun/DAILY 13:00)、下班(xiaoqiao/WEEKLY mask=62 周一~五 21:30)、科目四(Yunyang/DAILY 11:30)
- fire_log 基线前为 0(历史日志已按用户指令清空)

## 5. 与旧任务书记载的差异
| 旧描述 | 实际 |
|---|---|
| "1200+ 组测试" | 5 个测试方法,其中 randomCrossCheck1000 单方法含 **1000 组随机对拍**+4 专项(窗口/免打扰/一次性过期等),全绿 |
| 构建依赖 /tmp/gradle-8.5 | 属实;**项目内 gradlew 脚本存在但 wrapper jar/properties 缺失**,`./gradlew` 不可用(PHASE 3 待修) |
| 历史前端(像素 UI/背景视频/滚轮) | 已全部清除;git 历史已压平(单提交+baseline tag),旧 UI 只存在于用户 Downloads 资产与旧归档描述 |
| UI/Backend 契约 id | 像素版 8 id 已删,**现契约=5 个 id**:alertTitle/alertMessage/slideTrack/btnSlide/btnSnooze(res/values/ids.xml) |
| CLAUDE.md/规范文档 | 曾存在,已按用户指令删除(PHASE 1 本轮重建) |

## 6. 真机基线验证(2026-09-05,DBY2-W00)
- 装置:release 1.15MB 安装成功
- 60 秒真实触发(SQL 插 ONCE→release→保屏等待):fire_log = `基线测试, 13ms, coin+pack:daji, fired|screen=on|ui=shown|slide-ack`
- 结论:**闹钟链/音效/语音包/滑动确认全链路健康**,基线成立

## 7. 已识别风险(前 5)
1. `./gradlew` 不可用(wrapper 缺失),构建依赖 /tmp 临时 Gradle——环境脆弱
2. targetSdk=33 陈旧(2026 年现状),后续商店/系统兼容会倒逼升级;升级将牵动 exact alarm/FGS/通知行为,需单独阶段
3. versionCode=3/versionName=1.2.0 与实际 v10.3 语义脱节
4. release=debug 签名(用户已知惯例,换签名需授权+数据迁移)
5. AlertActivity 同时持有 2 分钟 timeout 与滑动确认,壳 UI 无恢复编辑入口(编辑/设置页已删,重构期功能缺口)
