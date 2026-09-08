package dev.evesis.ontime.ui.alert

import dev.evesis.ontime.ui.components.WorkspaceColumns
import dev.evesis.ontime.ui.components.WorkspacePanel
import dev.evesis.ontime.ui.components.WorkspacePage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.evesis.ontime.ui.components.PixelSwipeToConfirm
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import dev.evesis.ontime.ui.theme.OnTimeEyebrow
import dev.evesis.ontime.ui.theme.OnTimeHeroTitle
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/** 到点提醒：内容与行动分区；滑动确认和稍后契约保持。 */

@Composable
fun AlertScreen(
    title: String,
    message: String,
    voiceLabel: String,
    snoozeLabel: String,
    onSlideAck: () -> Unit,
    onSnooze: () -> Unit,
) {
    WorkspacePage("到时间了", "现在，留一点时间给这件事", actions = {}) {
        WorkspaceColumns(Modifier.weight(1f), leadingWeight = 1.3f, leading = {
            Column(Modifier.fillMaxWidth().padding(vertical = OnTimeSpacing.xxl)) {
                Text(voiceLabel, style = OnTimeEyebrow, color = OnTimeColors.VoiceCyan)
                Text(title, style = OnTimeHeroTitle, color = OnTimeColors.Gold,
                    modifier = Modifier.padding(vertical = OnTimeSpacing.xl))
                Text(message, style = OnTimeBodyLarge, color = OnTimeColors.InkWhite)
            }
        }, trailing = {
            WorkspacePanel("准备好了吗", "滑动确认本次提醒，或稍后再提醒") {
                PixelSwipeToConfirm(hint = "向右滑动 · 我知道了", onConfirm = onSlideAck,
                    modifier = Modifier.fillMaxWidth())
                QuietButton(onClick = onSnooze, text = snoozeLabel, modifier = Modifier.fillMaxWidth())
            }
        })
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Preview(name = "Phone", widthDp = 360, heightDp = 720)
@Composable
private fun AlertScreenPreview() {
    OnTimeTheme {
        AlertScreen(
            title = "喝水",
            message = "该喝水了,起来活动一下,补充水分。",
            voiceLabel = "妲己 说",
            snoozeLabel = "稍后 5 分钟",
            onSlideAck = {},
            onSnooze = {},
        )
    }
}
