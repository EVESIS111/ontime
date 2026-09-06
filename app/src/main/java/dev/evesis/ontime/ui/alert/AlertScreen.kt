package dev.evesis.ontime.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import dev.evesis.ontime.ui.components.PixelSwipeToConfirm
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeHeroTitle
import dev.evesis.ontime.ui.theme.OnTimeLayout
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/*
 * Alert v2:瞬时情绪场景(§28)——只留 提醒名/台词/滑动确认/稍后;大留白,零装饰框。
 */

@Composable
fun AlertScreen(
    title: String,
    message: String,
    snoozeLabel: String,
    onSlideAck: () -> Unit,
    onSnooze: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .padding(OnTimeSpacing.gutterExpanded),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Column(
            Modifier
                .widthIn(max = OnTimeLayout.controlMaxWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style = OnTimeHeroTitle,
                color = OnTimeColors.Gold,
                textAlign = TextAlign.Center,
            )
            Text(
                message,
                style = OnTimeBodyLarge,
                color = OnTimeColors.InkWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    top = OnTimeSpacing.lg,
                    bottom = OnTimeSpacing.sectionGap,
                ),
            )
            PixelSwipeToConfirm(
                hint = "滑动确认",
                onConfirm = onSlideAck,
                modifier = Modifier.fillMaxWidth(),
            )
            QuietButton(
                onClick = onSnooze,
                text = snoozeLabel,
                emphasize = false,
                modifier = Modifier
                    .padding(top = OnTimeSpacing.md)
                    .align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.padding(bottom = OnTimeSpacing.xxl))
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Composable
private fun AlertScreenPreview() {
    OnTimeTheme {
        AlertScreen(
            title = "喝水",
            message = "该喝水了,起来活动一下,补充水分。",
            snoozeLabel = "稍后 5 分钟",
            onSlideAck = {},
            onSnooze = {},
        )
    }
}
