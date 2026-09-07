# LAYOUT_INVARIANTS.md — 布局不变量(任何版本不得破坏;违例=commit 拒绝)

## Global
- 无控件重叠;无重复控件;无业务控件负 offset/任意 offset;无 scale 式适配
- 所有 clickable ≥48dp;所有内容在 safeDrawing 内
- 视口测试术语:模拟视口称 Forced/simulated viewport,不称真机测试

## 结构
- 唯一 OnTimeLayoutMode(SINGLE_PANE / DASHBOARD)——一个窗口只落一个 mode,禁布尔嵌套
- AdaptiveSpec 只供尺寸 token(gutter/hero/width);结构由 LayoutMode 决定
- 每屏每个语义控件恰好 1 个:保存/删除/取消/时间选择/音色选择/音效选择/列表
- 同类组件同尺寸(◀=▶;类型块=类型块;周块=周块;组件内部 padding,页面只排组件间)
- 横屏不复制数据:Home DASHBOARD=左(Hero+下一发)/右(列表);Editor=单列 720

## Screen Content Contract(摘要;全文见本文件 §下)
- HOME:时钟/日期/下一发摘要/列表/新增/设置/(异常警示)。禁:voice/sound 细节、完整台词
- EDITOR:标题/台词/重复/时间/周/间隔/窗口/日期/音色/音效/稍后/保存/删除/取消——各恰 1 次
- ALERT:角色眉标/提醒名/台词/滑动/稍后
- SETTINGS:运行状态/语音包/开发/版本

## 测试 Gate(每次 UI 改动)
- 语义计数:保存=1、时间=1、音色=1、音效=1、(Home)提醒眉标=1
- 触控 ≥48dp;旋转 continuity(编辑不丢);font1.3;长文本×2

## 本轮用户体验调整
按 V12_EXPERIENCE_SPEC.md 做小范围真机样张：编辑单列 760dp，上下固定操作区，低频项折叠；仍禁止重复字段、重叠、越界和小于48dp触控。视觉验收前不扩大页面重构。
