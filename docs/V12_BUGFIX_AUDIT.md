# v12 实机缺陷审计

用户反馈：删除键一直可见，存在多处运行异常。设备 DBY2-W00 在线；装机 11.4/code6，源码已含上轮本地候选。

| 编号 | 问题与证据 | 修复约束 | 状态 |
|---|---|---|---|
| F1 | 真机四行红色删除区透过开关；SwipeRevealRow 永久绘制底层而前景透明 | 关闭时无删除语义/触控，前景不透明，行裁剪；一次仅一行展开 | 已复现，待修 |
| F2 | Editor VM 属于 Activity；loadedId/finished 跨多次进入保留 | 按导航 entry 管理，旋转保留、退出重进新会话 | 源码确认 |
| F3 | 音色和音效试听同调用，音效优先导致“音色试听”只播音效 | 独立试听源、异步准备、离页停止、停止不报错 | 源码确认 |
| F4 | AlertScreen 只声明 pane composable lambda 从未调用 | 恢复真实内容，横竖屏都保留确认与稍后 | 源码确认，P0 |
| F5 | AlertActivity 未用 Receiver msg，UI 再随机、播放器读原始多句字符串 | 沿既有 msg/textOverride 传同一句，不改变提醒语义 | 源码确认 |
| F6 | Home/Settings 状态仅初始读取，返回权限页/提醒后不刷新 | 生命周期恢复刷新；通知检查覆盖 Android 12 | 源码确认 |
| F7 | 保存覆盖 audioUri 与 lastFiredAt 为 null/0；空标题静默失败、无效时间可保存 | 保留已有非编辑字段，给内联验证错误 | 源码确认 |
| F8 | MainActivity 每次启动 seedDefaultsIfEmpty，删除全部会重生示例 | 仅首次新库初始化默认项，不动当前用户数据 | 源码确认 |

参考：Android 官方 ViewModel scope、Compose graphics modifiers、MediaPlayer state/resources；复用已有依赖，不升级或新增生产依赖。
- https://developer.android.com/topic/libraries/architecture/viewmodel
- https://developer.android.com/develop/ui/compose/graphics/draw/modifiers
- https://developer.android.com/media/platform/mediaplayer/state-resources

验收：先构建/单测；覆盖安装后验证默认无红色、单行展开/收起、编辑反复进入/取消/保存/旋转、两类试听、到点弹窗/台词/slide-ack/fire_log；只清理本轮测试行。

- F9：真机 Editor 输入框横屏铺满 2800px；fillMaxSize 在 widthIn 前锁死约束。调整修饰符顺序，恢复既定 720dp 单列，不改视觉语言。

- F10：免打扰顺延分支直接调用 setExactAndAllowWhileIdle，缺少其他分支已有的 SecurityException 降级；撤销精确闹钟权限后会崩溃。仅补相同 setWindow 兜底，不改免打扰窗口/日志/调度算法；夜间实机分支尚未验证。

- F11：已有 DAILY/WEEKLY 的 atMillis=0，切换 ONCE 会显示 1970 年；切入 ONCE 且旧日期已过期时提供一小时后的默认值。

## 已完成与新增体验问题（2026-09-08）
F1–F9/F11 已修，F10 已补降级但夜间撤权场景仍待真机；代码提交3ec3c89、3bb3471。真实触发及删除证据见EXECUTION_STATE，不把源码修复等同所有场景通过。

- U1：ontimeHitArea 自定义layout丢弃父约束；改标准sizeIn。
- U2：表单字号小、保存藏在长滚动末端；分组、放大、固定操作区。
- U3：PixelButton内容继承黑色，深色背景不可读；显式提供正常金色/按下深蓝色。
- U4：按钮固定高度在大字体下可能裁切；改最小高度和内边距。
- U5：设置页横屏无最大宽度、返回位于底部、长导入结果挤进按钮；限宽、顶部返回、独立结果文本。
体验样张不是最终视觉验收，完整回归待设备空闲。
