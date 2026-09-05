package dev.evesis.ontime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.OnTimeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Channels.ensure(this)
        VoicePacks.installBundled(this)
        Sounds.ensureInstalled(this)
        Db.get(this).seedDefaultsIfEmpty()
        Alarms.scheduleAll(this)
        KeepAliveService.start(this)

        val list = Db.get(this).list()
        setContent {
            OnTimeTheme {
                HomeShell(reminders = list)
            }
        }
    }
}

/** 过渡壳(PHASE4c):验证 Compose 链路 + 数据直读;像素视觉与交互在 PHASE5/6 落地 */
@Composable
fun HomeShell(reminders: List<Reminder>) {
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
                ReminderRowShell(r)
            }
        }
    }
}

@Composable
private fun ReminderRowShell(r: Reminder) {
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
        Switch(checked = r.enabled, onCheckedChange = null)
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
