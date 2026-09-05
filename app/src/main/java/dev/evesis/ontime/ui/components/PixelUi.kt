package dev.evesis.ontime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSpacing

/**
 * OnTime 像素组件层(v9 视觉语言:2dp 金细框 + 硬角 + 半透明深蓝容器)。
 * 原则:交互底层全部沿用 M3 成熟组件(触摸目标/涟漪/语义/禁用态),只换皮肤。
 */

/** 像素面板:内容容器(提醒行/弹层/表单分组) */
@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.72f))
            .border(2.dp, OnTimeColors.Gold),
    ) {
        Column(Modifier.padding(OnTimeSpacing.md)) { content() }
    }
}

/** 像素按钮:M3 Button 换皮(硬角金框,按下由 M3 负责) */
@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = OnTimeColors.DeepBlueHigh.copy(alpha = 0.6f),
            contentColor = OnTimeColors.Gold,
        ),
        border = BorderStroke(2.dp, OnTimeColors.Gold),
        content = content,
    )
}

/** 金色细线分隔(弱化透明度,不抢内容) */
@Composable
fun PixelDividerText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.padding(horizontal = OnTimeSpacing.sm),
        color = OnTimeColors.Gold.copy(alpha = 0.55f),
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
    )
}
