package dev.evesis.ontime.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.evesis.ontime.ui.components.*
import dev.evesis.ontime.ui.theme.OnTimePageTitle
import dev.evesis.ontime.ui.theme.OnTimeDisplayTime
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import dev.evesis.ontime.ui.components.PixelGlyph
import dev.evesis.ontime.ui.components.PixelIcon
import androidx.compose.foundation.combinedClickable
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.LocalOnTimeAdaptive
import dev.evesis.ontime.ui.theme.OnTimeLayoutMode
import dev.evesis.ontime.ui.theme.layoutMode
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import dev.evesis.ontime.ui.components.PixelToggle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.data.AlarmHealth
import dev.evesis.ontime.data.AlarmHealthProbe
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeReminderTitle
import dev.evesis.ontime.ui.theme.OnTimeSecondary
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 首页：横屏概览与日程双栏，窄屏紧凑概览；保留既有删除和选择状态。 */

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEdit: (id: Long) -> Unit = {},
    onAdd: () -> Unit = {},
    onSettings: () -> Unit = {},
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    val context = androidx.compose.ui.platform.LocalContext.current
    HomeShell(
        reminders = state.reminders,
        onToggle = viewModel::setEnabled,
        onEdit = onEdit,
        onAdd = onAdd,
        onSettings = onSettings,
        alarmHealth = state.alarmHealth,
        onFixHealth = { AlarmHealthProbe.openSettings(context) },
        selecting = state.selecting,
        selectedIds = state.selectedIds,
        onBeginSelect = viewModel::beginSelect,
        onToggleSelect = viewModel::toggleSelect,
        onClearSelection = viewModel::clearSelection,
        onDeleteSelected = viewModel::deleteSelected,
        onDeleteOne = viewModel::deleteOne,
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
    selecting: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onBeginSelect: (Long) -> Unit = {},
    onToggleSelect: (Long) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onDeleteOne: (Long) -> Unit = {},
) {
    val now = rememberMinuteTick()
    val active = reminders.filter { it.enabled && it.nextFireAt > 0 }
    val next = active.minByOrNull { it.nextFireAt }
    val spec = LocalOnTimeAdaptive.current

    // 选择模式:返回键先退出选择(系统 Back 语义保留)
    BackHandler(enabled = selecting) { onClearSelection() }
    val overview: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.xl)) {
            Column(Modifier.padding(vertical = OnTimeSpacing.lg)) {
                Text(SimpleDateFormat("M月d日 · EEEE", Locale.CHINA).format(Date(now)),
                    style = OnTimeSecondary, color = OnTimeColors.InkMuted)
                Text(SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(now)),
                    style = OnTimeDisplayTime, color = OnTimeColors.InkWhite)
            }
            WorkspacePanel("下一条提醒", if (next == null) "现在没有待触发的提醒" else "${humanize(next.nextFireAt - now)}后") {
                next?.let { n ->
                    Text(n.title, style = OnTimePageTitle, color = OnTimeColors.Gold)
                    Text(n.desc(), style = OnTimeSecondary, color = OnTimeColors.InkMuted)
                    QuietButton(onClick = { onEdit(n.id) }, text = "查看这条提醒 →")
                }
            }
            if (alarmHealth != AlarmHealth.HEALTHY) WorkspacePanel("需要完成权限设置") {
                Text(if (alarmHealth == AlarmHealth.DEGRADED) "提醒可能延迟，请允许闹钟权限。" else "通知已关闭，请开启以显示提醒。",
                    style = OnTimeSecondary, color = OnTimeColors.InkWhite)
                QuietButton(onClick = onFixHealth, text = "去设置", emphasize = true)
            }
        }
    }
    val schedule: @Composable () -> Unit = {
        Column(Modifier.fillMaxSize().background(OnTimeColors.DeepBlueHigh).padding(OnTimeSpacing.xl)) {
            if (reminders.isEmpty()) {
                Text("把要记住的事交给准时", style = OnTimeBodyLarge, color = OnTimeColors.InkWhite)
                Text("点击右上角新建提醒，选择时间和声音。", style = OnTimeSecondary, color = OnTimeColors.InkMuted)
            } else ScheduleListSection(reminders, onEdit, onToggle, OnTimeSpacing.xs,
                selecting, selectedIds, onBeginSelect, onToggleSelect, onClearSelection,
                onDeleteSelected, onDeleteOne, onBeginSelect)
        }
    }
    WorkspacePage("准时", "${active.size} 条已开启 · 让每件事按时发生", actions = {
        QuietButton(onClick = onSettings, text = "设置")
        PixelButton(onClick = onAdd, modifier = Modifier.padding(start = OnTimeSpacing.lg)) {
            Text("＋ 新建", style = OnTimeButtonLabel)
        }
    }) {
        when (spec.layoutMode) {
            OnTimeLayoutMode.DASHBOARD -> Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.xxl)) {
                Column(Modifier.weight(0.8f).fillMaxHeight().verticalScroll(rememberScrollState())) { overview() }
                Box(Modifier.weight(1.3f).fillMaxHeight()) { schedule() }
            }
            OnTimeLayoutMode.SINGLE_PANE -> {
                // Compact overview leaves the main viewport to the reminder list.
                Row(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.xl), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(now)), style = OnTimePageTitle, color = OnTimeColors.InkWhite)
                        Text(SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(Date(now)), style = OnTimeSecondary, color = OnTimeColors.InkMuted)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("下一条", style = OnTimeSecondary, color = OnTimeColors.InkMuted)
                        Text(next?.let { "${it.title} · ${humanize(it.nextFireAt - now)}后" } ?: "暂无提醒",
                            style = OnTimeBodyLarge, color = OnTimeColors.Gold)
                    }
                }
                if (alarmHealth != AlarmHealth.HEALTHY) QuietButton(onClick = onFixHealth, text = "提醒权限需要设置 →")
                Box(Modifier.weight(1f)) { schedule() }
            }
        }
    }
}

/** 调度列表区(单/双栏共用,单一 Source of Truth §93) */
@Composable
private fun ScheduleListSection(
    reminders: List<Reminder>,
    onEdit: (Long) -> Unit,
    onToggle: (id: Long, on: Boolean) -> Unit,
    topGap: androidx.compose.ui.unit.Dp,
    selecting: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onBeginSelect: (Long) -> Unit = {},
    onToggleSelect: (Long) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onDeleteOne: (Long) -> Unit = {},
    onLongSelect: (Long) -> Unit = {},
) {
    var openId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(selecting, reminders.map { it.id }) { openId = null }
    BackHandler(enabled = !selecting && openId != null) { openId = null }
    if (selecting) {
        // 批量操作条(替换眉标;删除=通栏主操作)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = topGap, bottom = OnTimeSpacing.md),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                "已选 ${selectedIds.size}",
                style = OnTimeMetadata,
                color = OnTimeColors.Gold,
                modifier = Modifier.weight(1f),
            )
            dev.evesis.ontime.ui.components.QuietButton(onClick = onClearSelection, text = "取消")
            dev.evesis.ontime.ui.components.PixelButton(onClick = onDeleteSelected) {
                Text("删除", style = OnTimeButtonLabel)
            }
        }
    } else {
        Text(
            "提醒",
            style = OnTimeMetadata,
            color = OnTimeColors.Gold.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = topGap, bottom = OnTimeSpacing.md),
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(bottom = OnTimeSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.md),
    ) {
        items(reminders, key = { it.id }) { r ->
            if (selecting) {
                SelectRow(r, r.id in selectedIds) { onToggleSelect(r.id) }
            } else {
                SwipeRevealRow(
                    r = r, onEdit = onEdit, onToggle = onToggle, onDelete = onDeleteOne,
                    onLongSelect = onLongSelect, isOpen = openId == r.id,
                    onOpen = { openId = r.id }, onClose = { if (openId == r.id) openId = null },
                )
            }
        }
    }
}

/** 微信式左滑行:滑出红色删除按钮(吸附),点按钮才删除;点行收回/进编辑;长按进选择 */
@Composable
private fun SwipeRevealRow(
    r: Reminder,
    onEdit: (Long) -> Unit,
    onToggle: (id: Long, on: Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onLongSelect: (Long) -> Unit = {},
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val revealPx = with(density) { 96.dp.toPx() }
    var offsetX by remember(r.id) { mutableFloatStateOf(0f) }
    LaunchedEffect(isOpen, revealPx) { offsetX = if (isOpen) -revealPx else 0f }

    Box(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clipToBounds()) {
        // 关闭时不生成删除控件，避免透明穿透与隐藏按钮误触。
        if (offsetX < 0f) Box(
            Modifier
                .align(Alignment.CenterEnd)
                .width(96.dp)
                .fillMaxHeight()
                .background(OnTimeColors.Danger)
                .clickable(enabled = isOpen) { onClose(); onDelete(r.id) },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PixelIcon(
                    glyph = PixelGlyph.CLOSE,
                    sizeDp = 18, color = OnTimeColors.OnDanger,
                )
                Text("删除", style = OnTimeButtonLabel, color = OnTimeColors.OnDanger,
                    modifier = Modifier.padding(top = OnTimeSpacing.xxs))
            }
        }
        // 前层:行内容(跟手位移;松手吸附 按钮位/收回;点击行为随状态)
        Box(
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(OnTimeColors.DeepBlue)
                .pointerInput(revealPx, isOpen) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -revealPx / 2f) { offsetX = -revealPx; onOpen() }
                            else { offsetX = 0f; onClose() }
                        },
                        onDragCancel = { offsetX = if (isOpen) -revealPx else 0f },
                        onHorizontalDrag = { change, amt ->
                            change.consume()
                            offsetX = (offsetX + amt).coerceIn(-revealPx, 0f)
                        },
                    )
                },
        ) {
            ScheduleRow(
                r = r,
                onEdit = { if (isOpen || offsetX < 0f) { offsetX = 0f; onClose() } else onEdit(r.id) },   // 露出时点击=收回
                onToggle = { on -> offsetX = 0f; onClose(); onToggle(r.id, on) },
                onLongClick = { onLongSelect(r.id) },
            )
        }
    }
}

/** 选择模式行:整行勾选(时刻位换 ✓ 块) */
@Composable
private fun SelectRow(r: Reminder, selected: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggle)
            .padding(vertical = OnTimeSpacing.md),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        // 勾选块(替代时刻位;金底 ✓ / 暗框空)
        Box(
            Modifier
                .padding(end = OnTimeSpacing.lg)
                .size(40.dp)
                .background(if (selected) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
                .border(2.dp, if (selected) OnTimeColors.InkWhite else OnTimeColors.GoldDim.copy(alpha = 0.5f)),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            if (selected) Text("✓", style = OnTimeButtonLabel, color = OnTimeColors.DeepBlue)
        }
        Text(
            r.title,
            style = OnTimeReminderTitle,
            color = if (selected) OnTimeColors.Gold else OnTimeColors.InkWhite,
            modifier = Modifier.weight(1f),
        )
    }
}

/** time-first 行:左侧 40sp 像素时刻(视觉主体),右标题,开关末端;长按进选择模式 */
@Composable
private fun ScheduleRow(
    r: Reminder,
    onEdit: (Long) -> Unit,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = { onEdit(r.id) }, onLongClick = onLongClick)
        .padding(OnTimeSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = OnTimeSpacing.lg)) {
            Text(r.title, style = OnTimeBodyLarge,
                color = if (r.enabled) OnTimeColors.InkWhite else OnTimeColors.InkMuted)
            Text(if (r.enabled) "${r.rowTimeLabel()}  ·  ${r.desc()}" else "已暂停 · ${r.desc()}",
                style = OnTimeSecondary, color = if (r.enabled) OnTimeColors.Gold else OnTimeColors.InkMuted,
                modifier = Modifier.padding(top = OnTimeSpacing.sm))
        }
        PixelToggle(checked = r.enabled, onCheckedChange = onToggle)
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
