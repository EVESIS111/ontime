# 「准时」v12 本地交接 · 候选版

**本地收尾已完成；真机另约，未正式定版。** 本轮未操作平板、未安装 APK、未修改用户数据库或语音资产、未写设备归档、未创建 v12.0-final、未 push。

## 候选包

- 文件：OnTime-v12.0-candidate-cf81676.apk（3,752,367 bytes）。
- 版本：12.0 / versionCode 7；包名：dev.evesis.ontime。
- 源码提交：`cf81676203cb28cb3e1f737d0468b9728f36437b`；后续交接文档提交不改变该 APK。
- SHA256：`7d3b04828f716fabb73397a210c8fc6e401d79cecd5621a2efca1d5ef4042303`。
- 签名继续使用既有 Android Debug 证书；debug/release 证书一致。证书 SHA256：`6e4f78c527164c12c67155c942833174da00bffc93c344d89d371e066c2d7d48`。
- 已检查最终 APK DEX 含 cf81676，不含旧 d0da5ea；无 INTERNET 权限，release 非 debuggable。
- 本地归档：artifacts/v12/；设备 /sdcard/OnTime项目/v12/ 尚未写入。归档与源码不混存，APK 不入 Git。

## 已完成变更

- 先更新 CLAUDE.md、当前执行状态、历史文档标识和可靠性边界；保留旧验收历史。
- 修正备份/恢复的 .db-wal/.db-shm 配对；备份逐文件检查字节数；源库仅在临时副本上打开、合并和校验。
- 恢复前自动捕获设备当前数据；推完整快照、清空旧日志、逐字节回读及 SQLite integrity_check；任何失败停止。
- 版本与设置页脚使用实际构建信息；删除 14 条未使用 import。保留组件目录和有效 Preview，无视觉重做。
- ScheduleEngine、Alarms、Receivers、Room 数据库、Db、Reminder、fire_log 实体、AlertPlayer、VoicePacks 与 150023f 逐文件字节相同。
- app/src 与 tools 无 TODO/FIXME/XXX。docs/references/klokk/MainScreen.kt 的一项上游 TODO 属引用快照，未修改第三方参考历史。

## 本地验证

| 检查 | 结果 | 证据边界 |
|---|---|---|
| testDebugUnitTest | PASS，6 方法，0 失败/跳过 | 含 1000 组对拍、200 组窗口随机测试及台词选择；不是设备测试 |
| lint | PASS，0 Error / 18 Warning / 2 Hint | 未抑制、未加 baseline、未升级依赖 |
| assembleDebug / assembleRelease | PASS | 最终构建 33 秒；JDK 17.0.20 / 仓库 Gradle 9.4.1 |
| DB 工具 | PASS，10 项离线测试 | 模拟 adb；覆盖 WAL-only 数据、无 WAL、同条数不同内容、无效库、旧式命名、失败/短读、残留 WAL、回读不一致 |
| 包校验 | PASS | aapt 元数据、apksigner 验签、debug/release 证书对比、DEX 脚印、SHA256 |
| Shell / diff | PASS | bash -n；git diff --check |
| 四页截图、操作手感、声音 | UNVERIFIED | 本轮不接管设备，不用旧截图冒充 |
| recents / 一小时熄屏 / 24h 日常使用 | UNVERIFIED | 按用户选择另约 |

原始 build.log、db-tests.log、lint-results-debug.xml、verification.json 随候选包交付。本机完整单测报告在 app/build/reports/tests/testDebugUnitTest/。

## 剩余风险与延期

| 项目 | 判断与处理 |
|---|---|
| 真实使用与稳定性 | 本地通过不能证明平板到点可靠；设备及全天试用通过前不打 final tag |
| 数据库运维真机兼容 | 离线通过，华为 run-as/数据视图仍需实际复验；多文件恢复不具原子性，中途失败先用安全备份恢复，禁止带半成品启动 App |
| Lint 版本提示 | 11 条工具链/SDK/依赖提示，本期按要求冻结，不按提示自动升级 |
| Lint API 提示 | 1 条 Android 12 权限页常量提示；已有异常回退，唯一目标设备是 Android 12；低版本兼容不在本轮验收内 |
| Lint 其余提示 | Modifier 参数顺序 1、旧 SDK 判断 1、applicationContext 单例告警 1、KTX 建议 3、状态装箱提示 2；保留并记录，不借清理改核心或公共组件接口 |
| 既有签名 | 保持现有证书；这是当前平板本地安装候选包，未执行商店发行或签名迁移 |
| 历史布局 P3 | UI_BUG_INVENTORY #7 的 Home baseline 微差继续记录，未经视觉验收不打磨 |
| 用户事项与扩展 | id11「标题1」等回应；台词扩容、语音管理新界面、KMP/Desktop、时区/系统时间变更测试延期 |

## 下一次设备窗口

1. adb devices 确认 DBY2-W00；核对设备签名后仅 install -r。数据库流程读 DB_MAINTENANCE.md，维护完成立即装回 release 并核实闹钟自愈。
2. 开关→编辑→保存→60 秒真实触发→角色语音→slide-ack→fire_log→下一轮闹钟；仅用本轮测试事项验证两步删除与多选。
3. 四页与组件目录横竖屏、大字体/长文本、旋转连续性、48dp 触控、零重叠/越界；截图并让先生验收声音和手感。
4. 补 recents 划掉和至少一小时熄屏。熄屏期间不保屏；记录 deviceidle，没进入深度 idle 不能声称 Doze 通过；测完恢复设备设置。
5. 最终候选连续日常使用 24 小时，不换包、不停进程采证。记录初始计划及期间变更，逐条核对 fire_log 和实际体验。
6. 全部门槛通过后追加正式截图/结果，将同一 APK 与 SHA 新增至设备 v12/ 并回读校验；文件已存在则停止，不覆盖。更新状态、干净提交后打 v12.0-final。
