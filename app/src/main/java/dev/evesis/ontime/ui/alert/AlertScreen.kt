package dev.evesis.ontime.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.evesis.ontime.ui.components.PixelSwipeToConfirm
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import dev.evesis.ontime.ui.theme.OnTimeEyebrow
import dev.evesis.ontime.ui.theme.OnTimeHeroTitle
import dev.evesis.ontime.ui.theme.OnTimeLayout
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/*
 * Alert v3 — 结构:角色席位(§36 产品差异化)· 母版:游戏 Dialog × Klokk 负空间
 * 「妲己 说」眉标(角色色)→ 提醒名 Hero → 台词(引号对话排版)→ 滑动 → 稍后。
 */

@Composable
fun AlertScreen(
    title: String,
    message: String,
    voiceLabel: String,
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
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            Modifier
                .widthIn(max = OnTimeLayout.controlMaxWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 角色席位:眉标 + 角色色(声音是角色,画面给席位)
            Text(
                voiceLabel.uppercase(),
                style = OnTimeEyebrow,
                color = OnTimeColors.VoiceCyan,
            )
            Text(
                title,
                style = OnTimeHeroTitle,
                color = OnTimeColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = OnTimeSpacing.md),
            )
            // 台词:引号对话排版
            Text(
                "“$message”",
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
