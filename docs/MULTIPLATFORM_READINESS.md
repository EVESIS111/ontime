# MULTIPLATFORM_READINESS.md — 2026-09-06 审计(目标:Android + Windows + macOS)

> 方法:逐文件统计 `android.*`/`androidx.*` import;分层判定。原则:Readiness ≠ 立即迁移;
> Android 稳定优先,shared module 在网络与时间窗口允许时建立(本阶段 Deferred,见文末)。

## SHARED READY(可直接进 commonMain)
| 文件 | android imports | 说明 |
|---|---|---|
| ScheduleEngine.kt | 0 | 纯 Kotlin,1000 组对拍保护——**最高价值共享资产,原样迁移** |
| Reminder.kt | 3(全 room.*) | Room 注解为 KMP 兼容 API;模型+desc/pickMessage 纯逻辑 |
| FireLogEntity.kt | 4(room.*) | 同上 |
| OnTimeDatabase.kt | 10(room.*+sqlite) | Room 2.8 KMP 支持 DAO/Database;仅 builder 需平台 expect |
| HomeViewModel.kt | 1(lifecycle) | lifecycle-viewmodel 本身是 KMP 库 |
| Theme.kt / Type.kt | compose | androidx.compose→org.jetbrains.compose 前缀切换即可(色板/字号/Token 零平台依赖) |

## SHARED CANDIDATE(业务应共享,少量 Android API 待换)
| 文件 | 阻碍 | 迁移方式 |
|---|---|---|
| ReminderRepository.kt | android.content.Context(1) | 构造注入平台 provider(Db 已是唯一消费者) |
| EditorViewModel.kt | Context(列音色目录) | 音色列表改为 Repository 提供的平台接口 |
| PixelUi.kt / PixelSwipeToConfirm.kt / 各 Screen | androidx.compose + LocalContext/haptics | CMP(compose-multiplatform)前缀切换;LocalContext 调用点极少(3 处) |

## ANDROID PLATFORM(本质平台,永不共享)
Alarms.kt(AlarmManager 降级链)/ Receivers.kt(BOOT/TIME/权限广播)/ AlertActivity.kt(锁屏全屏)/ AlertPlayer.kt(AudioTrack/TTS)/ MainActivity.kt / Sounds.kt+VoicePacks.kt(filesDir/assets 文件)/ Db.kt(Room builder)/ AlarmHealth.kt(canScheduleExactAlarms 探针;**枚举 AlarmHealth 本身 shared**)

## DO NOT SHARE
无(无"为共享反而复杂"的候选)

## 结论与路径
1. **共享度评估:核心域(ScheduleEngine+模型+DAO+VM+DesignToken)≈ 全部 shared-ready**,UI 层 90% CMP 兼容;真平台层(Alarm/通知/音频)本就该按平台适配——符合"Product Core + Shared UI + Platform Adapter"目标。
2. **本阶段执行**:代码已按上述边界组织(包结构 data/ui 分层,Context 只出现在 Repository/VM 边界)。
3. **Deferred:kmp `shared` module + desktopApp smoke**——需要引入 kotlin-multiplatform 插件 + JetBrains CMP 依赖矩阵 + Gradle 结构改造(app→androidApp);当前网络(aliyun 间歇不可达)与 Android 可靠性优先级下风险大于收益。条件具备时按 §26 顺序迁:models→ScheduleEngine→tokens→CMP 组件→repository contracts。
4. 字体:res/font 的 ttf 迁 shared 时改 CMP 资源目录即可(文件已 subset,659KB)。
