package dev.evesis.ontime.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeSizing
import dev.evesis.ontime.ui.theme.OnTimeSpacing

/*
 * OnTimePixelKit(v11.2)—— Instant Pixel Utility
 * 规则:零动画(点击/开关/输入即时);零涟漪;硬角;纯色;按压=状态切换非插值(内容下沉 1 单位);
 * Pixel Unit = 2dp(边框/位移/阶梯全部 2 的倍数)。
 */

enum class PixelGlyph(val rows: List<String>) {
    // 5×7 点阵,'#'=亮(绘制单位=Pixel Unit 网格;§54 正式图标,禁字符/emoji 顶替)
    PREV(listOf("    #","   ##","  ###"," ####","  ###","   ##","    #")),
    NEXT(listOf("#    ","##   ","###  ","#### ","###  ","##   ","#    ")),
    ADD(listOf("  #  ","  #  ","#####","  #  ","  #  ","     ","     ")),
    SETTINGS(listOf(" ### ","#   #","# # #","#   #"," ### ","     ","     ")),
    PLAY(listOf("#    ","##   ","###  ","#### ","###  ","##   ","#    ")),
    STOP(listOf("#####","#   #","#   #","#   #","#####","     ","     ")),
    BACK(listOf("  #  "," #   ","#    "," #   ","  #  ","     ","     ")),
    CLOSE(listOf("#   #"," # # ","  #  "," # # ","#   #","     ","     ")),
    CHECK(listOf("    #","   ##","#  ##"," ## #","    #","     ","     ")),
    WARNING(listOf("  #  ","  #  "," # # "," # # ","#####","     ","     ")),
}

/** 像素图标:点阵绘制,视觉可小;命中由外层 ontimeHitArea 保障(视觉/命中分离) */
@Composable
fun PixelIcon(glyph: PixelGlyph, modifier: Modifier = Modifier, sizeDp: Int = 20, color: Color = OnTimeColors.Gold) {
    val density = LocalDensity.current
    val cell = with(density) { (sizeDp.dp.toPx() / 5f).toInt().coerceAtLeast(1) }
    val wDp = with(density) { (cell * 5).toDp() }
    val hDp = with(density) { (cell * 7).toDp() }
    val cellPx = cell.toFloat()
    Canvas(modifier.size(wDp, hDp)) {
        glyph.rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                if (c == '#') drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(x * cellPx, y * cellPx),
                    size = androidx.compose.ui.geometry.Size(cellPx, cellPx),
                )
            }
        }
    }
}

/** OnTimeTouchTarget(v11.3):命中区 ≥48dp,视觉元素居中不变大(视觉/命中分离) */
fun Modifier.ontimeHitArea(minDp: Int = 48): Modifier =
    this.sizeIn(minWidth = minDp.dp, minHeight = minDp.dp)

/** 像素按钮:正常=金框透明底;按下=金底+内容下沉 1 单位;禁用=暗框。零涟漪零动画。 */
@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .heightIn(min = OnTimeSizing.buttonHeight)
            .background(if (pressed && enabled) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
            .border(
                OnTimeSizing.borderFocused,
                when { !enabled -> OnTimeColors.GoldDim.copy(alpha = 0.4f); pressed -> OnTimeColors.InkWhite; else -> OnTimeColors.Gold },
                RectangleShape,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,          // 无涟漪
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .offset { IntOffset(0, if (pressed) OnTimeSizing.pixelUnit.roundToPx() else 0) }   // 按压下沉 1 像素单位
                .padding(horizontal = OnTimeSpacing.xl, vertical = OnTimeSpacing.sm),
            horizontalArrangement = Arrangement.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides
                if (pressed && enabled) OnTimeColors.DeepBlue else OnTimeColors.Gold) {
                content()
            }
        }
    }
}

/** 次级文字按钮(静默) */
@Composable
fun QuietButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    emphasize: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .ontimeHitArea()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = OnTimeButtonLabel,
            color = when {
                pressed -> OnTimeColors.Gold
                emphasize -> OnTimeColors.Gold.copy(alpha = 0.85f)
                else -> OnTimeColors.InkMuted
            },
        )
    }
}

/** 像素开关:方轨+方滑块,点击即时换位(零动画);开=金,关=暗 */
@Composable
fun PixelToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .ontimeHitArea()      // hit ≥48dp,视觉/命中分离(v11.3)
            .size(width = 72.dp, height = 40.dp)
            .background(if (checked) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.08f))
            .border(
                OnTimeSizing.borderFocused,
                when {
                    pressed -> OnTimeColors.InkWhite                       // 按下即亮(v11.3 反馈)
                    checked -> OnTimeColors.InkWhite
                    else -> OnTimeColors.GoldDim.copy(alpha = 0.5f)
                },
                RectangleShape,
            )
            .clickable(interactionSource = interaction, indication = null, role = Role.Switch, onClick = { onCheckedChange(!checked) }),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        // 滑块方块:开=右+深蓝带孔感,关=左+灰;pressed 时加亮边
        Box(
            Modifier
                .padding(OnTimeSizing.borderFocused)
                .size(32.dp)
                .background(if (checked) OnTimeColors.DeepBlue else OnTimeColors.InkMuted),
        )
    }
}

/** 像素步进器:◀ 值 ▶(替代 Material TimePicker 对话框;点即变) */
@Composable
fun PixelStepper(
    label: String,
    value: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    valueStyle: TextStyle = OnTimeButtonLabel,
    valueColor: Color = OnTimeColors.InkWhite,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (label.isNotEmpty()) {
            Text(label, style = OnTimeMetadata, color = OnTimeColors.InkMuted, modifier = Modifier.weight(1f))
        }
        StepArrow(text = "◀", onClick = onPrev)
        Text(
            value,
            style = valueStyle,
            color = valueColor,
            modifier = Modifier.padding(horizontal = OnTimeSpacing.lg),
        )
        StepArrow(text = "▶", onClick = onNext)
    }
}

@Composable
private fun StepArrow(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        Modifier
            .ontimeHitArea()
            .size(OnTimeSizing.minTouchTarget)
            .background(if (pressed) OnTimeColors.InkWhite.copy(alpha = 0.12f) else Color.Transparent)
            .border(OnTimeSizing.hairline, OnTimeColors.GoldDim.copy(alpha = 0.4f), RectangleShape)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PixelIcon(
            glyph = if (text == "◀") PixelGlyph.PREV else PixelGlyph.NEXT,
            color = if (pressed) OnTimeColors.Gold else OnTimeColors.InkMuted,
        )
    }
}

/** 像素文本输入:静态标签+底亮线(无 M3 label 浮动动画/无指示动画) */
@Composable
fun PixelTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    placeholder: String = "",
) {
    Column(modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) Text(label, style = OnTimeMetadata, color = OnTimeColors.InkMuted)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = OnTimeBodyLarge.copy(color = OnTimeColors.InkWhite),
            cursorBrush = SolidColor(OnTimeColors.Gold),
            minLines = minLines,
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = OnTimeSpacing.xs, bottom = OnTimeSpacing.sm)
                        .border(OnTimeSizing.hairline, OnTimeColors.Gold.copy(alpha = 0.25f))
                        .padding(OnTimeSpacing.md)
                        .heightIn(min = OnTimeSizing.minTouchTarget - OnTimeSpacing.md),
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(placeholder, style = OnTimeBodyLarge, color = OnTimeColors.InkMuted)
                    }
                    inner()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OnTimeSpacing.xs),
        )
    }
}

/** 无框静默面(列表行/分组底) */
@Composable
fun QuietSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.background(OnTimeColors.InkWhite.copy(alpha = 0.05f))) {
        Column(Modifier.padding(OnTimeSpacing.lg)) { content() }
    }
}

/** 焦点面板(全屏金框预算之一) */
@Composable
fun FocusPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.72f))
            .border(OnTimeSizing.borderFocused, OnTimeColors.Gold, RectangleShape),
    ) {
        Column(Modifier.padding(OnTimeSpacing.compIntLg)) { content() }
    }
}

/** 章节眉标 */
@Composable
fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(bottom = OnTimeSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label.uppercase(), style = dev.evesis.ontime.ui.theme.OnTimeEyebrow, color = OnTimeColors.Gold.copy(alpha = 0.8f))
        Box(
            Modifier
                .padding(start = OnTimeSpacing.md)
                .weight(1f)
                .height(OnTimeSizing.hairline)
                .background(OnTimeColors.Gold.copy(alpha = 0.25f)),
        )
    }
}


/** 像素图标钮:字符图标视觉小,命中区 ≥48dp(v11.3 §9/§10;替代散落的 emoji/小箭头) */
@Composable
fun PixelIconButton(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .ontimeHitArea()
            .background(if (pressed) OnTimeColors.InkWhite.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val glyph = PixelGlyph.entries.firstOrNull { it.name == icon }
        if (glyph != null) {
            PixelIcon(glyph = glyph, color = if (pressed) OnTimeColors.Gold else OnTimeColors.InkMuted)
        } else {
            Text(icon, style = OnTimeButtonLabel, color = if (pressed) OnTimeColors.Gold else OnTimeColors.InkMuted)
        }
    }
}
