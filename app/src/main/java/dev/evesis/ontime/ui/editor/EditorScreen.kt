package dev.evesis.ontime.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.Sounds
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.SectionHeader
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import dev.evesis.ontime.ui.theme.OnTimeBody
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeHeroTime
import dev.evesis.ontime.ui.theme.OnTimeLayout
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeScreenTitle
import dev.evesis.ontime.ui.theme.OnTimeSizing
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Editor v2(§26-27):Section 分组=大呼吸;组内紧凑;以眉标+hairline 分组,不以框分组。
 * 功能契约不变:四重复类型/周多选/窗口/音色音效/稍后/保存/删除。
 */

@Composable
fun EditorScreen(viewModel: EditorViewModel, id: Long, onDone: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(id) { viewModel.load(id, context) }
    LaunchedEffect(state.finished) { if (state.finished) onDone() }

    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .verticalScroll(rememberScrollState())
            .padding(
                top = OnTimeSpacing.xxl,
                bottom = OnTimeSpacing.gutter,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = OnTimeSpacing.xl)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuietButton(onClick = onDone, text = "← 取消")
                if (state.id > 0) {
                    QuietButton(onClick = viewModel::delete, text = "删除")
                }
            }
            Text(
                if (state.id == 0L) "新提醒" else "编辑提醒",
                style = OnTimeScreenTitle, color = OnTimeColors.InkWhite,
                modifier = Modifier.padding(top = OnTimeSpacing.md),
            )

            // ── 内容 ─────────────────────────────
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGapInner + OnTimeSpacing.xxl))
            SectionHeader("内容")
            PixelTextField(
                label = "标题", value = state.title,
                onValueChange = { v -> viewModel.update { it.copy(title = v) } },
            )
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            PixelTextField(
                label = "台词(多句用 | 分隔)", value = state.message, minLines = 2,
                onValueChange = { v -> viewModel.update { it.copy(message = v) } },
            )

            // ── 计划 ─────────────────────────────
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("计划")
            Row(
                horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm),
                modifier = Modifier.padding(bottom = OnTimeSpacing.lg),
            ) {
                ScheduleEngine.RepeatType.entries.forEach { t ->
                    val selected = state.repeatType == t
                    Text(
                        t.label,
                        style = OnTimeButtonLabel,
                        color = if (selected) OnTimeColors.DeepBlue else OnTimeColors.Gold,
                        modifier = Modifier
                            .background(if (selected) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
                            .border(if (selected) OnTimeSizing.borderFocused else OnTimeSizing.hairline, OnTimeColors.Gold.copy(alpha = if (selected) 1f else 0.3f))
                            .clickable { viewModel.update { it.copy(repeatType = t) } }
                            .padding(horizontal = OnTimeSpacing.lg, vertical = OnTimeSpacing.sm),
                    )
                }
            }
            when (state.repeatType) {
                ScheduleEngine.RepeatType.DAILY, ScheduleEngine.RepeatType.WEEKLY -> {
                    TimeField(state.timeOfDay) { m -> viewModel.update { it.copy(timeOfDay = m) } }
                    if (state.repeatType == ScheduleEngine.RepeatType.WEEKLY) {
                        Spacer(Modifier.padding(top = OnTimeSpacing.md))
                        WeekPicker(state.weekMask) { bit, on ->
                            viewModel.update { it.copy(weekMask = if (on) it.weekMask or bit else it.weekMask and bit.inv()) }
                        }
                    }
                }
                ScheduleEngine.RepeatType.INTERVAL -> {
                    NumberField("间隔(分钟)", state.intervalMinutes) { v -> viewModel.update { it.copy(intervalMinutes = v.coerceIn(1, 1440)) } }
                    Spacer(Modifier.padding(top = OnTimeSpacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (state.windowStart >= 0)
                                "只在 %02d:%02d - %02d:%02d".format(
                                    state.windowStart / 60, state.windowStart % 60,
                                    state.windowEnd / 60, state.windowEnd % 60)
                            else "全天任意时段",
                            style = OnTimeMetadata, color = OnTimeColors.InkMuted,
                            modifier = Modifier.weight(1f),
                        )
                        QuietButton(
                            onClick = {
                                viewModel.update {
                                    if (it.windowStart >= 0) it.copy(windowStart = -1, windowEnd = -1)
                                    else it.copy(windowStart = 9 * 60, windowEnd = 21 * 60 + 30)
                                }
                            },
                            text = if (state.windowStart >= 0) "清除窗口" else "设窗口",
                        )
                    }
                }
                ScheduleEngine.RepeatType.ONCE -> {
                    DateTimeField(state.atMillis) { t -> viewModel.update { it.copy(atMillis = t) } }
                }
            }

            // ── 体验 ─────────────────────────────
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("体验")
            CyclerField(
                label = "音色",
                text = state.voiceId.ifEmpty { "跟随系统" },
                onPrev = { viewModel.update { it.copy(voiceId = cycle(it.availableVoices, it.voiceId, -1)) } },
                onNext = { viewModel.update { it.copy(voiceId = cycle(it.availableVoices, it.voiceId, +1)) } },
            )
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            CyclerField(
                label = "音效",
                text = Sounds.byId(state.soundId).label,
                onPrev = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, it.soundId, -1)) } },
                onNext = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, it.soundId, +1)) } },
            )

            // ── 行为 ─────────────────────────────
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("行为")
            NumberField("稍后提醒(分钟)", state.snoozeMinutes) { v -> viewModel.update { it.copy(snoozeMinutes = v.coerceIn(1, 60)) } }

            // ── 保存(唯一通栏主操作;取消/删除已在顶部)─────────────
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            PixelButton(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text("保存", style = OnTimeButtonLabel)
            }
            Spacer(Modifier.padding(top = OnTimeSpacing.xxl))
        }
    }
}

private val ScheduleEngine.RepeatType.label: String
    get() = when (this) {
        ScheduleEngine.RepeatType.DAILY -> "每天"
        ScheduleEngine.RepeatType.WEEKLY -> "每周"
        ScheduleEngine.RepeatType.INTERVAL -> "间隔"
        ScheduleEngine.RepeatType.ONCE -> "一次"
    }

private fun <T> cycle(list: List<T>, current: T, dir: Int): T {
    if (list.isEmpty()) return current
    val i = list.indexOf(current).let { if (it < 0) 0 else it }
    return list[Math.floorMod(i + dir, list.size)]
}

/** 填充式输入(静默底+下边线;取代 v1 金框 OutlinedTextField) */
@Composable
private fun PixelTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
) {
    Column {
        Text(label, style = OnTimeMetadata, color = OnTimeColors.InkMuted)
        androidx.compose.material3.TextField(
            value = value, onValueChange = onValueChange,
            minLines = minLines,
            textStyle = OnTimeBodyLarge,
            shape = RectangleShape,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedTextColor = OnTimeColors.InkWhite,
                unfocusedTextColor = OnTimeColors.InkWhite,
                focusedContainerColor = OnTimeColors.InkWhite.copy(alpha = 0.06f),
                unfocusedContainerColor = OnTimeColors.InkWhite.copy(alpha = 0.04f),
                focusedIndicatorColor = OnTimeColors.Gold,
                unfocusedIndicatorColor = OnTimeColors.Gold.copy(alpha = 0.25f),
                cursorColor = OnTimeColors.Gold,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = OnTimeSpacing.xs),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(minutes: Int, onPick: (Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    Text(
        "%02d:%02d".format(minutes / 60, minutes % 60),
        style = OnTimeHeroTime,
        color = OnTimeColors.InkWhite,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(vertical = OnTimeSpacing.sm),
    )
    if (showPicker) {
        val tp = rememberTimePickerState(initialHour = minutes / 60, initialMinute = minutes % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = { onPick(tp.hour * 60 + tp.minute); showPicker = false }) {
                    Text("确定", color = OnTimeColors.Gold)
                }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("取消", color = OnTimeColors.InkMuted) } },
            title = { Text("选择时间", style = OnTimeBody) },
            text = { TimePicker(state = tp) },
            containerColor = OnTimeColors.DeepBlueHigh,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeField(atMillis: Long, onPick: (Long) -> Unit) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(atMillis) }
    val cal = Calendar.getInstance().apply { timeInMillis = atMillis }
    Column {
        Text(
            SimpleDateFormat("M月d日", Locale.CHINA).format(Date(atMillis)),
            style = OnTimeBody, color = OnTimeColors.InkMuted,
        )
        Text(
            SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(atMillis)),
            style = OnTimeHeroTime,
            color = OnTimeColors.InkWhite,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDate = true }
                .padding(vertical = OnTimeSpacing.sm),
        )
    }
    if (showDate) {
        val dp = rememberDatePickerState(initialSelectedDateMillis = atMillis)
        AlertDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dp.selectedDateMillis?.let { pickedDate = it + cal.get(Calendar.HOUR_OF_DAY) * 3_600_000L + cal.get(Calendar.MINUTE) * 60_000L }
                    showDate = false; showTime = true
                }) { Text("下一步", color = OnTimeColors.Gold) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("取消", color = OnTimeColors.InkMuted) } },
            title = { Text("选择日期", style = OnTimeBody) },
            text = { DatePicker(state = dp) },
            containerColor = OnTimeColors.DeepBlueHigh,
        )
    }
    if (showTime) {
        val tp = rememberTimePickerState(initialHour = cal.get(Calendar.HOUR_OF_DAY), initialMinute = cal.get(Calendar.MINUTE), is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val c = Calendar.getInstance().apply { timeInMillis = pickedDate }
                    c.set(Calendar.HOUR_OF_DAY, tp.hour); c.set(Calendar.MINUTE, tp.minute); c.set(Calendar.SECOND, 0)
                    onPick(c.timeInMillis); showTime = false
                }) { Text("确定", color = OnTimeColors.Gold) }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("取消", color = OnTimeColors.InkMuted) } },
            title = { Text("选择时间", style = OnTimeBody) },
            text = { TimePicker(state = tp) },
            containerColor = OnTimeColors.DeepBlueHigh,
        )
    }
}

@Composable
private fun WeekPicker(weekMask: Int, onToggle: (bit: Int, on: Boolean) -> Unit) {
    val names = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm)) {
        names.forEachIndexed { i, name ->
            val bit = 1 shl i
            val on = weekMask and bit != 0
            Text(
                name,
                style = OnTimeButtonLabel,
                color = if (on) OnTimeColors.DeepBlue else OnTimeColors.InkMuted,
                modifier = Modifier
                    .background(if (on) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
                    .clickable { onToggle(bit, !on) }
                    .padding(horizontal = OnTimeSpacing.lg, vertical = OnTimeSpacing.sm),
            )
        }
    }
}

@Composable
private fun NumberField(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = OnTimeBody, color = OnTimeColors.InkMuted, modifier = Modifier.weight(1f))
        QuietButton(onClick = { onChange(value - if (value <= 15) 1 else 5) }, text = "−")
        Text(
            " $value ",
            style = OnTimeBodyLarge, color = OnTimeColors.InkWhite,
        )
        QuietButton(onClick = { onChange(value + if (value < 15) 1 else 5) }, text = "+")
    }
}

@Composable
private fun CyclerField(label: String, text: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = OnTimeBody, color = OnTimeColors.InkMuted, modifier = Modifier.weight(1f))
        QuietButton(onClick = onPrev, text = "◀")
        Text(" $text ", style = OnTimeBodyLarge, color = OnTimeColors.Gold)
        QuietButton(onClick = onNext, text = "▶")
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Composable
private fun EditorSkeletonPreview() {
    OnTimeTheme {
        Column(Modifier.background(OnTimeColors.DeepBlue)) {
            SectionHeader("计划")
            WeekPicker(0b0111110) { _, _ -> }
        }
    }
}
