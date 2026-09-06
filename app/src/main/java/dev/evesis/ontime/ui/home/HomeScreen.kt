package dev.evesis.ontime.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.data.AlarmHealth
import dev.evesis.ontime.data.AlarmHealthProbe
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.QuietSurface
import dev.evesis.ontime.ui.components.SectionHeader
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeHeroTime
import dev.evesis.ontime.ui.theme.OnTimeLayout
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeSecondary
import dev.evesis.ontime.ui.theme.OnTimeSectionTitle
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Home v2(Visual Redesign;参考:Klokk 时间主体 · Clock(yassineAbou) 信息层级 · Tomato 留白)
 * 层级:①当前时间(Hero,唯一最大元素)②下一发提醒 ③提醒列表(静默面无框)④操作。
 */

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEdit: (id: Long) -> Unit = {},
    onAdd: () -> Unit = {},
    onSettings: () -> Unit = {},
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    HomeShell(
        reminders = state.reminders,
        onToggle = viewModel::setEnabled,
        onEdit = onEdit,
        onAdd = onAdd,
        onSettings = onSettings,
        alarmHealth = state.alarmHealth,
        onFixHealth = { AlarmHealthProbe.openSettings(context) },
    )
}

/** 每分钟步进的时钟(Klokk 启发:时间是页面唯一主角) */
@Composable
private fun rememberMinuteTick(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            val nextMinute = (now / 60_000L + 1) * 60_000L
            delay((nextMinute - System.currentTimeMillis()).coerceIn(1, 60_000L))
            now = System.currentTimeMillis()
        }
    }
    return now
}

@Composable
fun HomeShell(
    reminders: List<Reminder>,
    onToggle: (id: Long, on: Boolean) -> Unit = { _, _ -> },
    onEdit: (id: Long) -> Unit = {},
    onAdd: () -> Unit = {},
    onSettings: () -> Unit = {},
    alarmHealth: AlarmHealth = AlarmHealth.HEALTHY,
    onFixHealth: () -> Unit = {},
) {
    val now = rememberMinuteTick()

    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .padding(
                top = OnTimeSpacing.heroTop,
                bottom = OnTimeSpacing.gutter,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .widthInMax(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ① Hero 时间
            Text(
                SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(now)),
                style = OnTimeHeroTime,
                color = OnTimeColors.InkWhite,
            )
            Text(
                SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(Date(now)),
                style = OnTimeSecondary,
                color = OnTimeColors.InkMuted,
                modifier = Modifier.padding(top = OnTimeSpacing.xs),
            )

            // ② 下一发(无计划时安静收起)
            val active = reminders.filter { it.enabled && it.nextFireAt > 0 }
            active.minByOrNull { it.nextFireAt }?.let { next ->
                Text(
                    "下一发 「${next.title}」 · ${humanize(next.nextFireAt - now)}后",
                    style = OnTimeMetadata,
                    color = OnTimeColors.Gold,
                    modifier = Modifier.padding(top = OnTimeSpacing.lg),
                )
            }

            // 健康警示(克制;点击修复)
            if (alarmHealth != AlarmHealth.HEALTHY) {
                Text(
                    if (alarmHealth == AlarmHealth.DEGRADED) "⚠ 提醒可能无法准时触发 · 点击修复"
                    else "⚠ 通知权限缺失 · 点击修复",
                    style = OnTimeSecondary,
                    color = OnTimeColors.Gold,
                    modifier = Modifier
                        .padding(top = OnTimeSpacing.sm)
                        .clickable(onClick = onFixHealth),
                )
            }

            // ③ 列表
            Column(Modifier.padding(top = OnTimeSpacing.sectionGap)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OnTimeSpacing.xl),
                ) {
                    SectionHeader("我的提醒")
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.md)) {
                        items(reminders, key = { it.id }) { r ->
                            ReminderRowV2(r, onEdit) { on -> onToggle(r.id, on) }
                        }
                    }
                    if (reminders.isEmpty()) {
                        Text(
                            "还没有提醒。点下面的按钮,建第一条。",
                            style = OnTimeSecondary,
                            color = OnTimeColors.InkMuted,
                            modifier = Modifier.padding(top = OnTimeSpacing.lg),
                        )
                    }
                }
            }

            // ④ 操作(底部,主按钮限宽)
            Spacer(Modifier.weight(1f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OnTimeSpacing.xl),
                horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PixelButton(
                    onClick = onAdd,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("+ 新提醒", style = OnTimeButtonLabel)
                }
                QuietButton(onClick = onSettings, text = "⚙ 设置", emphasize = false)
            }
        }
    }
}

private fun Modifier.widthInMax(): Modifier = this.then(
    Modifier.padding(horizontal = OnTimeSpacing.gutter)
)

/** 提醒行 v2:无框静默面;信息三级(名称→规则/下次→开关);声音等细节留在编辑页(§25) */
@Composable
private fun ReminderRowV2(r: Reminder, onEdit: (Long) -> Unit, onToggle: (Boolean) -> Unit) {
    QuietSurface(
        Modifier
            .fillMaxWidth()
            .clickable { onEdit(r.id) },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    r.title,
                    style = OnTimeSectionTitle,
                    color = if (r.enabled) OnTimeColors.InkWhite else OnTimeColors.InkMuted,
                )
                Text(
                    buildString {
                        append(r.desc())
                        if (r.enabled && r.nextFireAt > 0) {
                            append(" · ")
                            append(SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(r.nextFireAt)))
                        }
                    },
                    style = OnTimeMetadata,
                    color = OnTimeColors.InkMuted,
                    modifier = Modifier.padding(top = OnTimeSpacing.xs),
                )
            }
            Switch(
                checked = r.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(start = OnTimeSpacing.md),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = OnTimeColors.Gold,
                    checkedThumbColor = OnTimeColors.DeepBlue,
                    uncheckedTrackColor = OnTimeColors.InkWhite.copy(alpha = 0.12f),
                    uncheckedThumbColor = OnTimeColors.InkMuted,
                ),
            )
        }
    }
}

private fun humanize(ms: Long): String {
    val m = (ms / 60_000L).coerceAtLeast(0)
    return when {
        m >= 60 -> "${m / 60} 小时 ${m % 60} 分"
        m >= 1 -> "$m 分钟"
        else -> "不到 1 分钟"
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Preview(name = "Phone", widthDp = 360, heightDp = 720)
@Composable
private fun HomeShellPreview() {
    OnTimeTheme {
        HomeShell(
            reminders = listOf(
                Reminder(1, "喝水", "该喝水了,起来活动一下,补充水分。", null, "daji", "coin",
                    ScheduleEngine.RepeatType.INTERVAL, 0, 0, 45, 0, 5, true, 1788700000000, 0, 540, 1290),
                Reminder(2, "补充水分并起来活动一下", "英语学习时间到了,坚持每天进步一点点,长台词测试行高。", null, "zhaojun", "powerup",
                    ScheduleEngine.RepeatType.DAILY, 13 * 60, 0, 60, 0, 5, false, 0, 0),
            )
        )
    }
}
