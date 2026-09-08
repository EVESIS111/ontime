# 全前端重构 / 2026-09-08

用户要求整个前端大优化，替代v12.0.2小修方向。产品仍是本地角色语音提醒。

## 视觉与结构
- 安静的深蓝提醒台；暖白主文字、金色主操作、蓝灰辅助。像素只用于品牌/少量时间标识，表单与按钮使用清晰sans。
- 统一页面标题、摘要、操作区、两级面板；不再全页散落小按钮与超宽字段。
- 横屏expanded：Home左概览右日程；Editor左内容/声音右时间/更多设置；Alert左内容右行动；Settings左运行/导入右关于/组件目录。
- 竖屏按同一顺序单列，不复制控件；可滚动区域与操作区分离。
- 编辑时间直接用系统TimePickerDialog，日期DatePickerDialog，避免几十次加减；选择音色显示明确列表，不再盲点循环箭头。保留现有状态及保存验证。
- >=48dp，主操作56dp，文字容器可增长；零过渡；用户实际数据与语音资产不动。

## 参考板（结构参考，无源码复制，无新依赖）
全局/编辑/设置：Android Supporting Pane https://developer.android.com/develop/adaptive-apps/guides/build-a-supporting-pane-layout
首页/导航/设置：Reply https://github.com/android/compose-samples （Apache-2.0）
全局/Alert：https://github.com/android/adaptive-apps-samples （Apache-2.0）
编辑：官方time pickers https://developer.android.com/develop/ui/compose/components/time-pickers
提醒/列表职责：项目已有Brutus、Dose、AOSP DeskClock参考板；只沿用职责分区，外观不复制。

## 验证
本地单测/lint/debug/release；最终实际截图与触控、横竖屏、键盘、长文本、大字体、保存删除和到点核心链。设备有未保存草稿则禁止安装/退出，继续本地工作；未做项目标未验证。旧APK保留可回退，不打final tag。
