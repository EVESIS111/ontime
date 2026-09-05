# ARCHITECTURE.md

## 现状(v10.3 基线)
单 Activity×2(View 体系纯代码 UI)+ 红区核心 9 文件:
ScheduleEngine(纯函数域逻辑)/Alarms+Channels/Receivers/AlertPlayer/Sounds/VoicePacks/Db(SQLite v3)/MainActivity+AlertActivity(壳 146 行)
零 AndroidX、零第三方、R8、release=debug 签名、1.15MB

## 目标(渐进)
android/architecture-templates:base 方向:单模块 Compose+Material3(交互层)+OnTime Pixel DesignSystem(视觉)+ViewModel/UDF/StateFlow+Repository+Room(迁移自 SQLite,带 Migration)+DataStore(设置项)+Navigation+Hilt(价值成立时)。ScheduleEngine 作为 domain 保留。

## 迁移原则
旧核心(Engine/Alarm/Receiver/Audio/VoicePack)不动,新 UI→新 ViewModel/Repository 边界→现有稳定核心;一次一个变量,每步真机回归。
