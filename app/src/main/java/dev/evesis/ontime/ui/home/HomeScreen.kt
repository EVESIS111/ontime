package dev.evesis.ontime.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.data.AlarmHealth
import dev.evesis.ontime.data.AlarmHealthProbe
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeHeroTime
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeReminderTitle
import dev.evesis.ontime.ui.theme.OnTimeRowTime
import dev.evesis.ontime.ui.theme.OnTimeSecondary
import dev.evesis.ontime.ui.theme.OnTimeSizing
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeSpotlightTitle
import dev.evesis.ontime.ui.theme.OnTimeTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Home v3 — Structural Redesign(2026-09-06 纠偏轮)
 * 母版:Klokk(Hero 时间巨物+负空间)· material-clock(time-first 行,MIT/README 自证)
 * 结构:①120sp Hero 时钟 ②Next Spotlight 横幅(全屏唯一金框)③time-first 调度列表
 *      ④右下角浮动新增 + 左下角设置(取消底部通栏按钮条)。
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

/** 行左侧的"下一次时刻"(INTERVAL 无单点时刻时显示间隔) */
private fun Reminder.rowTimeLabel(): String = when {
    enabled && nextFireAt > 0 ->
        SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(nextFireAt))
    repeatType == ScheduleEngine.RepeatType.INTERVAL -> "${intervalMinutes}′"
    else -> "·"
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
    val active = reminders.filter { it.enabled && it.nextFireAt > 0 }
    val next = active.minByOrNull { it.nextFireAt }

    Box(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = OnTimeSpacing.gutterExpanded),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = OnTimeSpacing.heroTop),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ① Hero 时钟(唯一居中元素;页面视觉支配)
                Text(
                    SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(now)),
                    style = OnTimeHeroTime,          // 120sp 巨物
                    color = OnTimeColors.InkWhite,
                )
                Text(
                    SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(Date(now)),
                    style = OnTimeSecondary,
                    color = OnTimeColors.InkMuted,
                    modifier = Modifier.padding(top = OnTimeSpacing.sm),
                )
            }

            // ② Next Spotlight(第二主体;左对齐横幅,全屏唯一 2dp 金框)
            next?.let { n ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = OnTimeSpacing.xxl)
                        .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.72f))
                        .border(OnTimeSizing.borderFocused, OnTimeColors.Gold)
                        .clickable { onEdit(n.id) }
                        .padding(horizontal = OnTimeSpacing.lg, vertical = OnTimeSpacing.md),
                ) {
                    Text("下一发", style = OnTimeMetadata, color = OnTimeColors.Gold)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = OnTimeSpacing.xs),
                    ) {
                        Text(n.title, style = OnTimeSpotlightTitle, color = OnTimeColors.Gold)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${humanize(n.nextFireAt - now)}后",
                            style = OnTimeSpotlightTitle,
                            color = OnTimeColors.InkWhite,
                            textAlign = TextAlign.End,
                        )
                    }
                    Text(
                        n.desc(),
                        style = OnTimeSecondary,
                        color = OnTimeColors.InkMuted,
                        modifier = Modifier.padding(top = OnTimeSpacing.xxs),
                    )
                }
            }

            // 健康警示(极克制一行)
            if (alarmHealth != AlarmHealth.HEALTHY) {
                Text(
                    if (alarmHealth == AlarmHealth.DEGRADED) "⚠ 提醒可能无法准时触发 · 点击修复"
                    else "⚠ 通知权限缺失 · 点击修复",
                    style = OnTimeSecondary,
                    color = OnTimeColors.Gold,
                    modifier = Modifier
                        .padding(top = OnTimeSpacing.md)
                        .clickable(onClick = onFixHealth),
                )
            }

            // ③ time-first 调度列表(大时间主导行;左对齐扫描)
            Text(
                "提醒",
                style = OnTimeMetadata,
                color = OnTimeColors.Gold.copy(alpha = 0.8f),
                modifier = Modifier.padding(
                    top = if (next != null) OnTimeSpacing.sectionGap else OnTimeSpacing.xxl,
                    bottom = OnTimeSpacing.md,
                ),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.xxs)) {
                items(reminders, key = { it.id }) { r ->
                    ScheduleRow(r, onEdit) { on -> onToggle(r.id, on) }
                }
            }
        }

        // ④ 浮动新增(右下角像素金块)
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(OnTimeSpacing.xl)
                .size(OnTimeSizing.fabSize)
                .background(OnTimeColors.Gold)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", style = OnTimeButtonLabel, color = OnTimeColors.DeepBlue)
        }
        // 设置入口(左下角,静默)
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(OnTimeSpacing.xl)
                .size(OnTimeSizing.fabSize)
                .background(OnTimeColors.InkWhite.copy(alpha = 0.07f))
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            Text("⚙", style = OnTimeButtonLabel, color = OnTimeColors.InkMuted)
        }
    }
}

/** time-first 行:左侧 40sp 像素时刻(视觉主体),右标题,开关末端 */
@Composable
private fun ScheduleRow(r: Reminder, onEdit: (Long) -> Unit, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onEdit(r.id) }
            .padding(vertical = OnTimeSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            r.rowTimeLabel(),
            style = OnTimeRowTime,     // 40sp 像素
            color = if (r.enabled) OnTimeColors.Gold else OnTimeColors.InkMuted.copy(alpha = 0.4f),
        )
        Text(
            r.title,
            style = OnTimeReminderTitle,   // 20sp 像素,与 40sp 时间形成主次落差
            color = if (r.enabled) OnTimeColors.InkWhite else OnTimeColors.InkMuted,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = OnTimeSpacing.lg),
        )
        Switch(
            checked = r.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = OnTimeColors.Gold,
                checkedThumbColor = OnTimeColors.DeepBlue,
                uncheckedTrackColor = OnTimeColors.InkWhite.copy(alpha = 0.12f),
                uncheckedThumbColor = OnTimeColors.InkMuted,
            ),
        )
    }
}

private fun humanize(ms: Long): String {
    val m = (ms / 60_000L).coerceAtLeast(0)
    return when {
        m >= 60 -> "${m / 60}小时${m % 60}分"
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
                Reminder(1, "喝水", "该喝水了。", null, "daji", "coin",
                    ScheduleEngine.RepeatType.INTERVAL, 0, 0, 45, 0, 5, true, 1788700000000, 0, 540, 1290),
                Reminder(2, "补充水分并起来活动一下", "英语学习时间到了,坚持每天进步一点点,长台词行高测试。", null, "zhaojun", "powerup",
                    ScheduleEngine.RepeatType.DAILY, 13 * 60, 0, 60, 0, 5, true, 1788690000000, 0),
            )
        )
    }
}
