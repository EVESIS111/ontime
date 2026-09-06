# DESIGN_SYSTEM.md — OnTime Design System v2(2026-09-06 Visual Redesign)

> 风格定位:**Premium Pixel Time Companion** —— Pixel/Game UI/Elegant/Blue/Gold/Quiet/Spacious/Time-focused/Retro-futuristic/Restrained。
> 方法来源:Token 工程=jenga(Apache-2.0);间距哲学=Carbon 2x Grid;时间主体=Klokk;信息层级=yassineAbou/Clock;质量基准=Tomato(GPL 只读)。
> 硬规则:布局代码禁止裸写 fontSize/dp(装饰性 2dp 描边例外需注释);层级手段优先级=Spacing>Opacity>Typography>Background>Alignment>Border。

## 1. Color(继承 v1,新增透明度语义)
| Token | 值 | 用途 |
|---|---|---|
| DeepBlue | #101B2E | 全局底(非纯黑) |
| DeepBlueHigh | #18263D | 焦点面板/对话框底 |
| Gold | #E3C068 | 焦点框(全局≤2 处)/Hero 强调/开关 |
| GoldDim | #9A8551 | 未选边线/禁用 |
| InkWhite | #EDE6D6 | 主文字 |
| InkMuted | #8E9BB3 | 次文字 |
| VoiceCyan | #7FB4D8 | 角色信息点缀 |
| **面透明度规则** | InkWhite 4%(输入底)/5%(静默行)/6%(输入焦点) | 用透明度做层级,不新增颜色 |

## 2. Typography(双字体架构)
**Pixel 字体(Fusion Pixel 子集)= 身份**:HeroTime(80/100,+2sp)、HeroTitle(50/64,+4sp)、ScreenTitle(40/52,+3sp)、SectionTitle(30/42,+2sp)、ButtonLabel(20/28,+3sp)。像素字只允许 10 的倍数字号;字距全部为正(中文像素字需要呼吸,v1 的 0 字距是拥挤主因之一)。
**Sans(系统默认)= 可读性**:BodyLarge(18/30)、Body(15/24)、Secondary(13/21)、Metadata(13/20,+0.5sp)、Eyebrow(12/16,+2.5sp 全大写)、Caption(11/17)。正文换 sans 后像素标题反而更突出(Jenga display/body 分工)。
**行高**:全部 ≥1.4 倍;多行文案(台词/说明)按 ×1.5 预留,容忍文本 +50%。

## 3. Spacing(4dp 基准 + 五层语义)
尺度:none 0/xxs 2/xs 4/sm 8/md 12/lg 16/xl 24/xxl 32/xxxl 48/xxxxl 64。
语义层(布局只允许引用语义名):compInt=sm(组件内)、compIntLg=lg(大组件)、compGap=md(组件间)、rowGap=md(列表行)、**sectionGap=xxxl(功能组间——v2 呼吸主角)**、gutter=xl(屏缘)、gutterExpanded=xxxl、heroTop=xxxl(首屏到 Hero)。

## 4. Vertical Rhythm(模板)
heroTop → Hero 时间 → xs → 日期 → lg → 下一发 → **sectionGap** → 眉标+hairline → 列表(compGap) → … → **sectionGap** → 底部操作。禁止"每层都 8dp"的工程界面节奏。

## 5. Sizing / Border / Shape
minTouchTarget 48 / buttonHeight 56 / swipeThumb 64 / hairline 1dp / borderFocused 2dp(金色)。Shape=RectangleShape(硬角);圆角仅 M3 系统组件内部。

## 6. 框预算(核心整改)
**每屏同时最多 1-2 处 2dp 金框**(主按钮算 1)。容器分级:QuietSurface(无框,InkWhite 5%)< FocusPanel(2dp 金框,仅焦点/弹层)。v1 的"每行一金框"已废除。分组用 SectionHeader(Eyebrow+hairline)。

## 7. Width / Adaptive
断点(官方 WindowSizeClass):<600 compact / 600-840 medium / ≥840 expanded;818dp 设备属 medium/expanded 边界,禁止硬编码。contentMaxWidth=560(单列阅读宽,居中,两侧留白随窗口增长);controlMaxWidth=480(按钮/滑条)。结构变化:compact 单列;medium 单列+大边距;expanded 居中内容列+两侧留白(未来 list-detail)。

## 8. Content Tolerance(§15)
文本容器一律内容驱动(禁固定 height);标题允许两行;Metadata 自动 wrap;按钮文字增长→按钮横向到 controlMaxWidth 后 wrap。Stress Test 基准:标题×2(「补充水分并起来活动一下」)、台词+50%、fontScale 1.3。

## 9. Motion / Opacity(占位,下阶段)
脉冲提示/开关/按下反馈沿用 M3;新增动效须先 Reference(Compose animation 官方样本)。

## 10. Pixel Asset Rule
像素资产(Kenney 等 CC0)仅作视觉语言参考;嵌入需整数倍缩放验证;字符图标(» ◀ ▶ ⚙ − +)为当前像素风轻量方案。
