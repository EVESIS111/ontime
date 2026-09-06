package dev.evesis.ontime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeEyebrow
import dev.evesis.ontime.ui.theme.OnTimeSizing
import dev.evesis.ontime.ui.theme.OnTimeSpacing

/*
 * OnTime 组件 v2(Visual Redesign;框预算:全屏同时最多 1-2 处 2dp 金框)
 * 层级手段优先级(§19):Spacing > Opacity > Typography > Background > Alignment > Border。
 */

/** 无框静默面:列表行/设置分组/输入组的底(取代 v1 的"每个内容都装框") */
@Composable
fun QuietSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.background(OnTimeColors.InkWhite.copy(alpha = 0.065f)),
    ) {
        Column(Modifier.padding(OnTimeSpacing.lg)) { content() }
    }
}

/** 焦点面板:页面中唯一的强调容器(如健康异常/弹层);2dp 金框仅此类与 CTA 允许 */
@Composable
fun FocusPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.72f))
            .border(OnTimeSizing.borderFocused, OnTimeColors.Gold),
    ) {
        Column(Modifier.padding(OnTimeSpacing.compIntLg)) { content() }
    }
}

/** 主按钮(像素字+金框):一屏最多一个实心级强调 */
@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(OnTimeSizing.buttonHeight),
        enabled = enabled,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = OnTimeColors.DeepBlueHigh.copy(alpha = 0.6f),
            contentColor = OnTimeColors.Gold,
        ),
        border = BorderStroke(OnTimeSizing.borderFocused, OnTimeColors.Gold),
        content = content,
    )
}

/** 次级文字按钮(取消/稍后/删除:弱化,不占框预算) */
@Composable
fun QuietButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    emphasize: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(OnTimeSizing.buttonHeight),
        shape = RectangleShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (emphasize) OnTimeColors.Gold else OnTimeColors.InkMuted,
        ),
    ) {
        Text(text, style = OnTimeButtonLabel)
    }
}

/** 章节眉标:小号金 label + 右侧 hairline(§27:以空间/线/字分组,不以框分组) */
@Composable
fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(bottom = OnTimeSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label.uppercase(), style = OnTimeEyebrow, color = OnTimeColors.Gold.copy(alpha = 0.8f))
        Box(
            Modifier
                .padding(start = OnTimeSpacing.md)
                .weight(1f)
                .height(OnTimeSizing.hairline)
                .background(OnTimeColors.Gold.copy(alpha = 0.25f)),
        )
    }
}

/** 屏幕内容列:统一 gutter+居中+contentMaxWidth(§30;所有 Screen 一律经此) */
@Composable
fun OnTimeContentColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    useExpandedGutter: Boolean = false,
    content: @Composable () -> Unit,
) {
    val gutter = if (useExpandedGutter) OnTimeSpacing.gutterExpanded else OnTimeSpacing.gutter
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = gutter),
        horizontalAlignment = horizontalAlignment,
    ) {
        Column(
            Modifier
                .widthIn(max = dev.evesis.ontime.ui.theme.OnTimeLayout.contentMaxWidth)
                .fillMaxWidth(),
            horizontalAlignment = horizontalAlignment,
        ) {
            content()
        }
    }
}

/** 内部用:文本行包装(Material 组件兜底样式不再直接暴露给页面) */
@Composable
internal fun fallbackBody(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}
