# v12 体验调整样张规范

用户已否定当前各页面的小字号、对齐和交互组织，要求全面提升。先做 Editor + 共享组件样张，在真机展示后再按反馈铺开。

## 第一版选择
- 保留深蓝/金色、方角像素品牌、零过渡动画；像素用于标题/数字，正文和表单优先中文可读性。
- 编辑页单列，按“提醒什么 → 什么时候 → 用什么声音”组织；标题和台词在同组。固定顶部取消与底部保存；正文独立滚动，键盘弹出后仍能保存。
- 常用字段可直接操作；音效、稍后、删除放入明确的“更多设置”展开区，删除需要行内二次确认。
- 全局正文提升为 18sp/22sp，辅助说明至少 16sp；字段标签 20sp。像素字号仍保持 10 的倍数。
- 表单内容最大 760dp，统一边距 24dp、组间距 24dp。主按钮至少 56dp，所有触控至少 48dp。
- 时间小时/分钟分组，每组数字与自己的加减控件对应；重复类型等宽；音色采用“上一项 / 当前值 / 下一项 + 试听”，不再把三个按钮散布整行。
- 修复 ontimeHitArea 丢弃父约束的问题，改用 Compose sizeIn 保留约束，解决等宽控件的对齐根因。
- 不改提醒计算、数据库、语音哈希和通知契约；已验收 hotfix APK 留在 artifacts/v12-fixes/ 可回退。

## 参考板（只借结构，继续复用已有 Compose/Pixel 组件）
1. Android Scaffold：固定操作区与滚动正文职责分离。https://developer.android.com/develop/ui/compose/components/scaffold
2. Android accessibility：48dp 触控及清晰标签。https://developer.android.com/guide/topics/ui/accessibility/views/apps-views
3. Material text fields：静态标签、支持文本和可见错误（不采用默认 Material 皮肤）。https://github.com/material-components/material-web/blob/main/docs/components/text-field.md
4. 项目既有 Brutus/Dose 编辑页参考及 UI_REFERENCE_MAP；不引新依赖，不复制未核许可代码。

验收：新建/编辑横竖屏截图、1.3 字体、键盘/保存/取消、字段语义唯一、触控/重叠/越界。展示样张给用户，未视觉验收不声称质感定版。

## 设置页一致性修正
沿用编辑页的最大宽度和固定顶部返回；正文滚动。导入结果单独显示，按钮文案保持稳定，避免长错误文本撑坏按钮。PixelButton 使用最小高度而非固定高度，允许大字体自然增长。保留导入路径与命名提示，因它们是现有导入流程必需信息。
