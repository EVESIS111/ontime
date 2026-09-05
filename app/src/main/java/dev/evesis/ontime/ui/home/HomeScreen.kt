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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.ui.theme.OnTimeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 首页(PHASE4d:VM 数据流+开关交互;像素视觉 PHASE5/6 落地) */
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    HomeShell(
        reminders = state.reminders,
        onToggle = viewModel::setEnabled,
    )
}

@Composable
fun HomeShell(reminders: List<Reminder>, onToggle: (id: Long, on: Boolean) -> Unit = { _, _ -> }) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("准时", style = MaterialTheme.typography.displaySmall)
        Text(
            "已注册提醒 ${reminders.count { it.enabled }} 个",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reminders, key = { it.id }) { r ->
                ReminderRowShell(r, onToggle)
            }
        }
    }
}

@Composable
private fun ReminderRowShell(r: Reminder, onToggle: (id: Long, on: Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(r.title, style = MaterialTheme.typography.titleLarge)
            Text(
                r.desc(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (r.enabled && r.nextFireAt > 0) {
                Text(
                    "下次 " + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(r.nextFireAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Switch(checked = r.enabled, onCheckedChange = { onToggle(r.id, it) })
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
                    ScheduleEngine.RepeatType.INTERVAL, 0, 0, 45, 0, 5, true, 0, 0, 540, 1290),
                Reminder(2, "学英语", "英语学习时间到了。", null, "zhaojun", "powerup",
                    ScheduleEngine.RepeatType.DAILY, 13 * 60, 0, 60, 0, 5, false, 0, 0),
            )
        )
    }
}
