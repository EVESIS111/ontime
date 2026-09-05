# CLAUDE.md — OnTime 项目最高工程规范

任何 AI 进入本项目,先读本文件。

## 1. Reference-First
能参考就不原创,能复用就不重写。通用能力(架构/DB/导航/权限/闹钟/通知/生命周期/测试/构建)优先:①本项目已验证实现 → ②Android 官方文档 → ③`android/architecture-templates:base` → ④AOSP → ⑤许可兼容的成熟开源。只有「准时」独有体验(像素视觉/角色/声音/台词/交互/业务规则)允许原创。重要决策必须写 `docs/DECISIONS.md` 并注明参考来源。

## 2. 参考/许可
- Alarm 生命周期最高参考:AOSP DeskClock;现代实现:pepperonas/brutus(MIT);Reminder UI:waseefakhtar/dose-android(MIT);GPL 项目(reReminder/FossifyClock)只读研究禁止复制;许可不明=不用。
- 借鉴记录统一进 `docs/REFERENCES.md`(来源/许可证/借鉴内容/是否复制代码)。

## 3. 不可破坏的核心
`ScheduleEngine.kt` 是行为规格(1000 组异构对拍全绿),**禁止为优雅重写**,发现明确 bug 才动。Reminder→Alarm→Receiver→Audio→Alert 链路语义(含 fire_log 契约 slide-ack/snoozed/timeout)变更须用户批准。语音包=音色id×台词整句 sha1 绑定(filesDir/voices/),禁止换 hash/改目录/破坏旧包。

## 4. 审批边界(触碰必须先报告用户)
删除用户数据/资产;丢弃未提交内容;重置 Git;改包名/换签名;大改 DB schema;整体替换提醒核心;引入 GPL/AGPL 或许可不明代码;不可回滚的系统改造。其余非破坏性执行无需逐次请示。

## 5. 资产保护(只读)
`/Users/evesis/Downloads/提醒APP/` 与 `/sdcard/OnTime项目/`(含设计资料/语音包产物/角色参考原声/GPT-SoVITS底模)默认只读;需用时复制到工作目录。禁止删除/覆盖/批量整理/重命名/移动。

## 6. Git/DB 安全
小 commit 单一主题可回滚;禁 reset --hard/clean -fd/强制覆盖。动 DB 前:force-stop→备份(含 -wal/-shm)→操作→回推;严禁 fallbackToDestructiveMigration 用于真实库;禁 wildcard 删除。

## 7. 真机验收 > 编译
每个核心阶段必须:装 APK→真实创建 Reminder→等真实 AlarmManager 触发→弹窗+音效+语音→slide-ack/snooze→fire_log 证据→下一轮闹钟。禁止用"直接调 Receiver"冒充到点验收。设备:DBY2-W00;测试技巧(保屏/锁竖屏/60s SQL 插桩法)见 `docs/DEVICE_DBY2-W00.md`。

## 8. 构建与依赖
JDK17=~/jdk/Contents/Home;Gradle 8.5(/tmp/gradle-8.5,Mac 重启会丢→重下 services.gradle.org;PHASE3 将标准化为 ./gradlew)。依赖只取 stable,版本以官方来源实时查询为准,禁止凭记忆编版本;新增依赖先答"SDK/AndroidX/官方模板能不能解决"。

## 9. UI 方向
Android Reminder App with Pixel Game Interface——Compose 负责交互/无障碍/适配,OnTime Pixel Design System 负责外观;禁游戏引擎/WebView UI。视觉基调:Premium Pixel/Retro RPG/克制蓝+金/透明容器/内容居中/安全边距;避免 Material 默认脸/圆角白卡堆/纯黑死背景/大片色块/花哨边框。像素资产禁模糊缩放,必须截图验证整数倍清晰。视觉批量铺开前先出 Home+Alert 真机样张给用户过目。

## 10. 用户沟通
用户是设计师,中文,结论先行:结论→为什么→体验影响→风险→能否回滚。无网络优先:不加账号/云/Analytics/广告。
