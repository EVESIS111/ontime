package dev.evesis.ontime.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelPanel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 首页:提醒列表 + 新增入口(PHASE6 像素版第一稿) */
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    HomeShell(
        reminders = state.reminders,
        onToggle = viewModel::setEnabled,
    )
}

@Composable
fun HomeShell(
    reminders: List<Reminder>,
    onToggle: (id: Long, on: Boolean) -> Unit = { _, _ -> },
    onAdd: () -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(OnTimeSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 标题区(内容列收束,平板不摊大饼)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = OnTimeSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("准时", style = MaterialTheme.typography.displaySmall, color = OnTimeColors.Gold)
            val active = reminders.filter { it.enabled && it.nextFireAt > 0 }
            Text(
                active.minByOrNull { it.nextFireAt }?.let {
                    "下次 「${it.title}」" + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(it.nextFireAt))
                } ?: "暂无计划中的提醒",
                style = MaterialTheme.typography.bodyMedium,
                color = OnTimeColors.InkMuted,
            )
        }

        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(reminders, key = { it.id }) { r ->
                ReminderRowPixel(r) { on -> onToggle(r.id, on) }
            }
        }

        PixelButton(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OnTimeSpacing.lg),
        ) {
            Text("+ 新提醒", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReminderRowPixel(r: Reminder, onToggle: (Boolean) -> Unit) {
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.titleLarge, color = OnTimeColors.InkWhite)
                Text(
                    r.desc(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnTimeColors.InkMuted,
                )
                if (r.enabled && r.nextFireAt > 0) {
                    Text(
                        "下次 " + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(r.nextFireAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnTimeColors.Gold,
                    )
                }
            }
            Switch(
                checked = r.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = OnTimeColors.Gold,
                    checkedThumbColor = OnTimeColors.DeepBlue,
                    uncheckedTrackColor = OnTimeColors.DeepBlueHigh,
                    uncheckedThumbColor = OnTimeColors.InkMuted,
                ),
            )
        }
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Preview(name = "Phone", widthDp = 360, heightDp = 720)
@Composable
private fun HomeShellPreview() {
    OnTimeTheme {
        HomeShell(
            listOf(
                Reminder(1, "喝水", "该喝水了,起来活动一下,补充水分。", null, "daji", "coin",
                    ScheduleEngine.RepeatType.INTERVAL, 0, 0, 45, 0, 5, true, 1788700000000, 0, 540, 1290),
                Reminder(2, "学英语", "英语学习时间到了。", null, "zhaojun", "powerup",
                    ScheduleEngine.RepeatType.DAILY, 13 * 60, 0, 60, 0, 5, false, 0, 0),
            )
        )
    }
}
