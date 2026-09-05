# REFERENCES.md — 能力→参考映射(每项:当前实现/第一参考/许可证/采用方式)

| 能力 | 当前实现 | 第一参考 | 许可 | 采用方式 |
|---|---|---|---|---|
| 项目总体架构 | 单模块 View 体系+纯代码壳 | android/architecture-templates:base | Apache-2.0 | PHASE4 起以其为基准渐进迁移(Compose/VM/Nav/Repo) |
| 调度规则 | OnTime ScheduleEngine(自研,1000 组对拍) | 自有(业务核心) | — | 保留不重写 |
| AlarmManager/Exact | setAlarmClock+setExactAndAllowWhileIdle | Android Developers + AOSP DeskClock | Apache-2.0 | 现实现即官方路径;PHASE9 审计对照后定 |
| 现代闹钟全链路 | — | pepperonas/brutus | MIT | PHASE9 对照(boot/snooze/full-screen/R8) |
| Reminder CRUD UI | 待重构 | waseefakhtar/dose-android | MIT | PHASE7 编辑页参考 |
| Interval/窗口/TTS 行为 | 自有 | ProfessorQuantumUniverse/reReminder | GPL-3.0 | 只读研究,禁复制 |
| 时钟 UX | — | FossifyOrg/Clock | GPL-3.0 | 只读研究 |
| 像素 UI 素材 | 无 | Kenney UI Pack(Pixel Adventure 等) | CC0 | 素材基底,转译为 OnTime 视觉 |
| 中文字体 | Fusion Pixel(曾用,OFL,已随前端清除) | github.com/TakWolf/fusion-pixel-font | OFL-1.1 | 前端重构时重新引入(源:Downloads 或重下) |
| 测试结构 | JUnit4 纯 JVM | architecture-samples | Apache-2.0 | 沿用 JUnit,迁 Compose 后补 UI Test |
