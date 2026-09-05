package dev.evesis.ontime.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelSwipeToConfirm
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import androidx.compose.ui.tooling.preview.Preview

/** 到点提醒页(锁屏/亮屏弹出;行为契约在 AlertActivity,此处纯展示+回调) */
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
            .padding(OnTimeSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            Modifier
                .widthIn(max = OnTimeSpacing.contentMaxWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.displaySmall, color = OnTimeColors.Gold, textAlign = TextAlign.Center)
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = OnTimeColors.InkWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = OnTimeSpacing.md, bottom = OnTimeSpacing.xxl),
            )
            PixelSwipeToConfirm(
                hint = "滑动确认",
                onConfirm = onSlideAck,
                modifier = Modifier.fillMaxWidth(),
            )
            PixelButton(
                onClick = onSnooze,
                modifier = Modifier
                    .padding(top = OnTimeSpacing.md)
                    .fillMaxWidth(),
            ) {
                Text(snoozeLabel, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
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
