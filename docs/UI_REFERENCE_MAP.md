# UI_REFERENCE_MAP.md — 页面/组件 → 参考映射

> 规则(CLAUDE.md §1/§9):每个页面开工前先建 Reference Board(搜 3–5 个同类实现),
> 按 复用等级 Level1 成熟库 → Level2 移植源码(宽松许可) → Level3 结构模仿换皮 → Level4 自研 的顺序决策。
> 每行落地后回填 Result(最终用了什么、源文件位置)。GPL 项目只能出现在"只读研究"行,禁复制源码。

## 状态图例
⬜ 未开工 · 🔍 调研中 · ✅ 已落地

| Screen | Feature | Reference Project | Reference File/Component | License | Reuse Method | Adaptation | 状态 |
|---|---|---|---|---|---|---|---|
| Home | 提醒列表/卡片/开关/下一次时间 | Brutus;yassineAbou/Clock;Dose | alarm list / alarm card / enable switch | MIT / Apache-2.0 | Level3 结构+交互借鉴(候选 Level2) | 换 OnTime 像素皮+Db 契约 | ⬜ |
| Home | 左滑操作(删除/编辑) | 待搜:`Compose swipe to delete`(SwipeToDismissBox 等 M3 官方组件优先) | TBD | TBD | Level1 官方组件优先 | 像素 track/thumbnail | ⬜ |
| Home | Empty State | compose-samples / Dose | empty state pattern | Apache-2.0 / MIT | Level3 | OnTime 角色台词 | ⬜ |
| Editor | 页面结构/信息层级 | Brutus alarm editor;Dose reminder editor | editor screen | MIT | Level2/3 | 字段映射 OnTime rule(16 列) | ⬜ |
| Editor | 时间选择(滚轮/表盘) | 待搜:`Compose wheel picker` × M3 TimePicker 对比 | TBD | TBD | Level1 优先(M3 TimePicker),滚轮需求另评 | 像素皮 | ⬜ |
| Editor | 周多选 Weekday | Brutus/Dose repeat UI + 现有 v9 周多选逻辑(mask) | week selector | MIT | Level3 | 保留 mask=62 语义 | ⬜ |
| Editor | 声音/语音选择器 | 待搜:`Compose settings selector`;Dose | TBD | TBD | Level3 | Sounds/VoicePacks 契约 | ⬜ |
| Alert | 全屏弹窗结构 | AOSP DeskClock AlarmActivity;Brutus | full-screen alert | Apache-2.0 / MIT | Level3(职责划分参考 DeskClock) | 保留 slide-ack/snoozed/timeout 契约 | ⬜ |
| Alert | 滑动确认/滑动稍后 | Brutus slide-to-snooze | slide gesture | MIT | Level2 源码级复用候选 | ≥90% 阈值+fire_log 契约不变 | ⬜ |
| Settings | 设置页框架 | 待搜:`Compose settings screen` + M3 | TBD | TBD | Level1/3 | DataStore 项 | ⬜ |
| 全局 | 平板/手机自适应 | compose-samples(Reply);adaptive-apps-samples | list-detail / canonical layout | Apache-2.0 | Level3 pattern(Window Size Class) | DBY2-W00=expanded 验收设备 | ⬜ |
| 全局 | Design Tokens/组件体系 | Backpack;Komposto | token 组织/preview/screenshot test | Apache-2.0 | 架构参照(不采品牌视觉) | OnTimeDesignSystem 蓝金像素 | ⬜ |
| 全局 | 像素资产(按钮/面板/滑块/边框) | Kenney UI Pack(Pixel Adventure 等) | button/panel/slider 9-slice | CC0 | Level1 素材直接用 | 转译 OnTime 视觉;整数倍缩放 | ⬜ |

## 变更记录
- 2026-09-05 建立(前端 Reference-First 补充指令);参考池许可证全部 GitHub API 实测入 REFERENCES.md。
