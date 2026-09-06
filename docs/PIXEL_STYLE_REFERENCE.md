# PIXEL_STYLE_REFERENCE.md — OnTime 像素视觉规则(v11.2 Instant Pixel Utility)

> 定位:**Instant Pixel Utility**——高品质游戏菜单的视觉 × 系统工具的速度。
> 参考规则来源:Kenney Pixel UI Pack(CC0,比例语言)· PixKit(MIT,组件体系思想)· Phaser PixUI(MIT,整数缩放/crisp 规则)。不复制其实现。

## Pixel Unit
统一基础单位 = **2dp**。边框厚度/按压位移/阶梯角/硬阴影偏移/图标网格全部为它的倍数。

## Shape
一律硬角(RectangleShape)。禁:圆角卡片/CircleShape/胶囊。角需要强调时用阶梯(2×2 缺角),不用曲线。

## Border Token(全 App 仅三档)
| Token | 值 | 用途 |
|---|---|---|
| Thin | 1dp(hairline) | 分隔线/未选容器 |
| Regular | 2dp(GoldDim 40-50%) | 次级控件描边 |
| Focus | 2dp(Gold 实色) | 焦点(全屏 ≤2 处) |

## Shadow
**Hard Shadow**:右下 2dp 纯色位移(DeepBlack),零模糊零羽化。禁 elevation/soft shadow。

## Motion Policy(v11.2 核心)
Navigation/Page/Dialog/Editor/List/Alert 进入 = **NONE**(NavHost 四个 transition 显式 None)。
Button/Toggle/Selector/Stepper = **Instant State Change**(颜色/位移直切,无插值;按压=内容下沉 1 pixelUnit)。
允许的动态:①滑动条跟手(snapTo,零插值)②时钟每分钟数字变化(内容更新非动画)。其余一律禁止(脉冲/发光/视差/交错入场/弹跳 全禁)。

## Icon
禁 emoji 作正式图标(⚙→≡)。像素字符(» ◀ ▶ − + ≡)为过渡方案;后续统一 16/24/32 网格像素图标(CC0 资产优先)。

## Image/Bitmap
FilterQuality.None(nearest-neighbor);整数倍缩放;3× 截图验证边缘方块锐利。

## Typography
双字体:Pixel(身份:Hero/标题/数字/按钮)+ Sans(正文)。像素字 10 的倍数字号;全部正字距(见 Type.kt)。

## Palette(封闭集)
DeepBlue #101B6E 基底 / DeepBlueHigh / Gold #E3C068 / GoldDim / InkWhite / InkMuted / VoiceCyan(角色点睛)/ 警示=Gold 本色。新颜色需先扩此表。

## Component Kit(OnTimePixelKit = PixelUi.kt)
PixelButton(按压下沉)/ QuietButton / PixelToggle(方轨方块,即时)/ PixelStepper(◀▶)/ PixelTextField(静态标签+亮线)/ QuietSurface / FocusPanel / SectionHeader / PixelSwipeToConfirm(cell 进度,snap)。页面禁止直接用 M3 Button/Switch/TimePicker/DatePicker/AlertDialog。

## 状态语言
Selected=金底深蓝字 · Pressed=内容下沉+边亮 · Disabled=暗边 40% · Off(开关)=暗框暗块 · Warning=Gold 文字行。
