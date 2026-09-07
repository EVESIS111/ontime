# CLAUDE.md — OnTime 项目最高工程规范

任何 AI 进入本项目,先读本文件。

## 1. Reference-First(覆盖全项目,含前端)
能参考就不原创,能复用就不重写。通用能力(架构/DB/导航/权限/闹钟/通知/生命周期/测试/构建/**UI/Layout/组件/动画/手势/适配/测试**)优先:①本项目已验证实现 → ②Android 官方文档 → ③`android/architecture-templates:base` → ④AOSP / 官方 samples → ⑤许可兼容的成熟开源 → ⑥CC0 素材。只有「准时」独有体验(像素视觉/角色/声音/台词/业务规则/品牌)允许原创。
**复用等级(从严到宽):**Level1 直接用成熟库/组件 → Level2 直接移植 Apache/MIT/BSD 源码(留 copyright、记来源)→ Level3 结构/交互完全模仿成熟实现(换皮)→ Level4 前三者皆否定才允许自己写。每个 UI 功能写码前必答:有无成熟实现?能否直接用?能否复制改?能否借结构?禁止"AI 凭感觉画 UI"。
**搜索即功能:**收到需求先搜(官方 samples/GitHub 功能级搜索,如 `Compose swipe delete`/`Compose wheel picker`/`slide to confirm`),再选最成熟、最简单、许可友好、版本兼容的实现。禁止拿 300 行稳定开源代码"重新生成 260 行类似代码"。
重要决策必须写 `docs/DECISIONS.md` 并注明参考来源。

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
JDK17=~/jdk/Contents/Home;Gradle 9.4.1（仓库 ./gradlew），AGP 9.2.1 / Compose plugin 2.3.21；v12 冻结依赖，保留 compose-group-mapping 2.3.21 强制对齐。依赖只取 stable,版本以官方来源实时查询为准,禁止凭记忆编版本;新增依赖先答"SDK/AndroidX/官方模板能不能解决"。

## 9. UI 方向(Reference-First 前端)
Android Reminder App with Pixel Game Interface——Compose 负责交互/无障碍/适配,OnTime Pixel Design System 负责外观;禁游戏引擎/WebView UI。视觉基调:Premium Pixel/Retro RPG/克制蓝+金/透明容器/内容居中/安全边距;避免 Material 默认脸/圆角白卡堆/纯黑死背景/大片色块/花哨边框。像素资产禁模糊缩放,必须截图验证整数倍清晰。视觉批量铺开前先出 Home+Alert 真机样张给用户过目。
补充(前端复用细则):
- **Pixel Visual ≠ Pixel Interaction**:像素只管视觉身份,交互底层用成熟 Compose 组件/手势(如 Slider 换 track/thumb 成 PixelSlider),保留 Accessibility,禁为像素风重写触摸算法。
- **参考池(许可证已实测,见 REFERENCES.md/UI_REFERENCE_MAP.md)**:Brutus(MIT)、yassineAbou/Clock(Apache-2.0)、Dose(MIT)、compose-samples·Jetsnack/Reply/JetLagged(Apache-2.0)、adaptive-apps-samples(Apache-2.0)、Backpack/Komposto(Apache-2.0,只学 Design System 建法不采品牌视觉)、Kenney(CC0 素材)、reReminder/FossifyClock(GPL 只读)。允许把高匹配项目当 Mother Template 提取页面/组件改造,但禁无脑 fork 整项目(复制"最完整同时最小"单元)。
- **每个页面开工前建 Reference Board**:先搜 3–5 个同类实现定结构,再动手;每个页面/组件的参考映射记入 `docs/UI_REFERENCE_MAP.md`(Screen/Feature/Reference/License/Reuse Method/Adaptation)。
- **适配按 Window Size Class**(compact/medium/expanded)建布局策略,DBY2-W00(expanded)只是验收设备;禁硬编码 818dp。布局数字(padding/spacing/宽度)先立 token 再统一调,禁无依据像素级盲调。
- **核心组件建 Preview(手机+平板),视觉回归优先用官方 Screenshot Test 机制**,不靠用户发现跑偏。
- 禁照搬商业 App 品牌资产(插画/Logo/角色/专有字体);无 LICENSE 仓库=All Rights Reserved,禁复制。

## 10. 用户沟通
用户是设计师,中文,结论先行:结论→为什么→体验影响→风险→能否回滚。无网络优先:不加账号/云/Analytics/广告。

## 11. v12 收尾补充规范（2026-09-07）
- 用户决定先本地实现与验证，设备另约；真机及连续 24 小时日常验收前不建 v12.0-final，不声称定版通过。正式版保留组件目录和有效 Preview。
- 禁止 adb uninstall，只能同签名 install -r。DB 运维：force-stop→装 debug→tools/backup-device-db.sh→本地副本操作→tools/restore-device-db.sh→装回 release 并核实闹钟自愈。release 禁止 run-as 读库。
- 备份伴生文件必须命名为 <文件>.db-wal / <文件>.db-shm。恢复先在临时副本合并 WAL 为完整快照，备份设备当前库，再推快照并清空旧 WAL/SHM。传输或完整回读校验失败必须停止，保留恢复路径。
- 资产只读规则仅增加一项例外：v12 验收通过后允许向 /sdcard/OnTime项目/v12/ 新增归档；同名已存在则停止，不覆盖。
- tools/ 放运维工具与离线测试；work/ 放可清理中间文件（不入 Git）；backups/ 放设备备份（新增不入 Git，不自动清理）；artifacts/v12/ 放候选 APK 与校验信息；docs/ 放交接和验收证据。
- 历史基线只标记适用版本，不改写旧记录。当前状态见 EXECUTION_STATE.md。签名、包名、核心行为和语音资产冻结。
- 本地验证：JAVA_HOME=~/jdk/Contents/Home ./gradlew testDebugUnitTest lint assembleDebug assembleRelease。
