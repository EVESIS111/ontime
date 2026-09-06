# UI_BUG_INVENTORY.md — v11.4 全量审计(2026-09-06)

| # | 分类 | Screen/视口 | 问题 | 根因 | 严重度 | 状态 |
|---|---|---|---|---|---|---|
| 1 | A 重复 | Home 横屏 | 左右两栏各渲染一份提醒列表(用户报告) | 主栏 ScheduleListSection 无互斥保护(两个 if 非互斥,§39 预言模式) | P0 | ✅ 已修:OnTimeLayoutMode 枚举 + when 互斥;真机验证「提醒」眉标 1x |
| 2 | A 重复 | Editor 横屏 | 体验/行为组双栏各一份风险 | 同类布尔分支叠加 | P1 | ✅ 已修:横屏退单列 maxwidth 720(§35 简化优先),双栏代码删除 |
| 3 | G 适配 | Editor 旋转 | 旋转后未保存编辑被重置(标题/选值丢失) | LaunchedEffect(id) 旋转重跑 load() 覆盖 state | P0 | ✅ 已修:VM loadedId 幂等守卫;真机验证旋转后标题保留 |
| 4 | J 视觉一致 | Editor | Voice/Sound 两套 cycler 实现+怪重载(content lambda 拆两行)导致基线漂移 | 组件分叉(§24) | P1 | ✅ 已修:统一 OptionSelector(值行+◀/▶/试听镜像行) |
| 5 | C 对齐 | Editor | 同类字段 label/control 节奏不一致 | 各字段自己 padding | P1 | ✅ 已修:FieldRow 统一(label→control→compGap) |
| 6 | J 一致 | 全局 | 报告版本 v11.3.1 vs APK 11.1/5 不一致;脚印花硬编码 | 版本无单一来源 | P0 | ✅ 已修:versionName 11.4/code 6;BuildConfig.GIT_SHA 构建注入 |
| 7 | F 基线 | Home | Spotlight 标题与倒计时 baseline 微差 | Row Bottom 对齐下不同字形 | P3 | 记录,非阻塞 |
| 8 | H 裁切 | Editor 竖屏 | 保存键可能落入导航区 | 滚动列底部 padding 不足 | P1 | ✅ 已修:save+navigationBarsPadding |

**修复 7/8(1 项 P3 记录)。方法升级:单一 LayoutMode 枚举;duplicate 检查=语义计数(uiautomator text×n);不再以"0 overflow"冒充"0 layout bug"。**
