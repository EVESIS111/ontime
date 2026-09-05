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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.Sounds
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelPanel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 提醒编辑页(摘要式折叠:按重复类型渐进显示相关字段;v9 信息架构延续) */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
            .padding(OnTimeSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = OnTimeSpacing.contentMaxWidth).fillMaxWidth()) {
            Text(
                if (state.id == 0L) "新提醒" else "编辑提醒",
                style = MaterialTheme.typography.titleLarge, color = OnTimeColors.Gold,
                modifier = Modifier.padding(bottom = OnTimeSpacing.md),
            )

            PixelPanel(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.md)) {
                PixelTextField(
                    value = state.title, onValueChange = { v -> viewModel.update { it.copy(title = v) } },
                    label = "标题",
                )
                Spacer(Modifier.height(OnTimeSpacing.sm))
                PixelTextField(
                    value = state.message, onValueChange = { v -> viewModel.update { it.copy(message = v) } },
                    label = "台词(可多句,用 | 分隔)",
                    minLines = 2,
                )
            }

            PixelPanel(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.md)) {
                Text("重复", style = MaterialTheme.typography.titleMedium, color = OnTimeColors.Gold)
                Spacer(Modifier.height(OnTimeSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm)) {
                    ScheduleEngine.RepeatType.entries.forEach { t ->
                        val selected = state.repeatType == t
                        Text(
                            t.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected) OnTimeColors.DeepBlue else OnTimeColors.Gold,
                            modifier = Modifier
                                .background(if (selected) OnTimeColors.Gold else OnTimeColors.DeepBlueHigh.copy(alpha = 0.5f))
                                .border(2.dp, OnTimeColors.Gold)
                                .clickable { viewModel.update { it.copy(repeatType = t) } }
                                .padding(horizontal = OnTimeSpacing.sm, vertical = OnTimeSpacing.xs),
                        )
                    }
                }
                Spacer(Modifier.height(OnTimeSpacing.md))
                when (state.repeatType) {
                    ScheduleEngine.RepeatType.DAILY, ScheduleEngine.RepeatType.WEEKLY -> {
                        TimeField(
                            minutes = state.timeOfDay,
                            onPick = { m -> viewModel.update { it.copy(timeOfDay = m) } },
                        )
                        if (state.repeatType == ScheduleEngine.RepeatType.WEEKLY) {
                            Spacer(Modifier.height(OnTimeSpacing.md))
                            WeekPicker(
                                weekMask = state.weekMask,
                                onToggle = { bit, on ->
                                    viewModel.update { it.copy(weekMask = if (on) it.weekMask or bit else it.weekMask and bit.inv()) }
                                },
                            )
                        }
                    }
                    ScheduleEngine.RepeatType.INTERVAL -> {
                        NumberField(
                            label = "间隔(分钟)", value = state.intervalMinutes,
                            onChange = { v -> viewModel.update { it.copy(intervalMinutes = v.coerceIn(1, 1440)) } },
                        )
                        Spacer(Modifier.height(OnTimeSpacing.sm))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (state.windowStart >= 0) "窗口 %02d:%02d-%02d:%02d".format(
                                    state.windowStart / 60, state.windowStart % 60,
                                    state.windowEnd / 60, state.windowEnd % 60)
                                else "时间窗口(可选)",
                                style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.InkMuted,
                                modifier = Modifier.weight(1f),
                            )
                            PixelButton(onClick = {
                                viewModel.update {
                                    if (it.windowStart >= 0) it.copy(windowStart = -1, windowEnd = -1)
                                    else it.copy(windowStart = 9 * 60, windowEnd = 21 * 60 + 30)
                                }
                            }) { Text(if (state.windowStart >= 0) "清除" else "设置", style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                    ScheduleEngine.RepeatType.ONCE -> {
                        DateTimeField(
                            atMillis = state.atMillis,
                            onPick = { t -> viewModel.update { it.copy(atMillis = t) } },
                        )
                    }
                }
            }

            PixelPanel(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.md)) {
                Text("播报", style = MaterialTheme.typography.titleMedium, color = OnTimeColors.Gold)
                Spacer(Modifier.height(OnTimeSpacing.sm))
                CyclerField(
                    label = "音色",
                    text = state.voiceId.ifEmpty { "跟随系统" },
                    onPrev = { viewModel.update { it.copy(voiceId = cycle(it.availableVoices, it.voiceId, -1)) } },
                    onNext = { viewModel.update { it.copy(voiceId = cycle(it.availableVoices, it.voiceId, +1)) } },
                )
                Spacer(Modifier.height(OnTimeSpacing.sm))
                CyclerField(
                    label = "音效",
                    text = Sounds.byId(state.soundId).label,
                    onPrev = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, it.soundId, -1)) } },
                    onNext = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, it.soundId, +1)) } },
                )
                Spacer(Modifier.height(OnTimeSpacing.sm))
                NumberField(
                    label = "稍后提醒(分钟)", value = state.snoozeMinutes,
                    onChange = { v -> viewModel.update { it.copy(snoozeMinutes = v.coerceIn(1, 60)) } },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.md)) {
                PixelButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                    Text("取消", style = MaterialTheme.typography.titleMedium)
                }
                PixelButton(onClick = viewModel::save, modifier = Modifier.weight(2f)) {
                    Text("保存", style = MaterialTheme.typography.titleMedium)
                }
            }
            if (state.id > 0) {
                Spacer(Modifier.height(OnTimeSpacing.md))
                PixelButton(onClick = viewModel::delete, modifier = Modifier.fillMaxWidth()) {
                    Text("删除此提醒", style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.VoiceCyan)
                }
            }
            Spacer(Modifier.height(OnTimeSpacing.xl))
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

@Composable
private fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RectangleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OnTimeColors.InkWhite,
            unfocusedTextColor = OnTimeColors.InkWhite,
            focusedBorderColor = OnTimeColors.Gold,
            unfocusedBorderColor = OnTimeColors.GoldDim,
            focusedContainerColor = OnTimeColors.DeepBlueHigh.copy(alpha = 0.5f),
            unfocusedContainerColor = OnTimeColors.DeepBlueHigh.copy(alpha = 0.3f),
            focusedLabelColor = OnTimeColors.Gold,
            unfocusedLabelColor = OnTimeColors.InkMuted,
            cursorColor = OnTimeColors.Gold,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(minutes: Int, onPick: (Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    Text(
        "时间  %02d:%02d".format(minutes / 60, minutes % 60),
        style = MaterialTheme.typography.titleLarge, color = OnTimeColors.InkWhite,
        modifier = Modifier
            .fillMaxWidth()
            .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.5f))
            .border(2.dp, OnTimeColors.GoldDim)
            .clickable { showPicker = true }
            .padding(OnTimeSpacing.md),
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
            title = { Text("选择时间", color = OnTimeColors.InkWhite) },
            text = { TimePicker(state = tp) },
            containerColor = OnTimeColors.DeepBlueHigh,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeField(atMillis: Long, onPick: (Long) -> Unit) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf(atMillis) }
    val cal = Calendar.getInstance().apply { timeInMillis = atMillis }
    Text(
        "时间  " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(atMillis)),
        style = MaterialTheme.typography.titleLarge, color = OnTimeColors.InkWhite,
        modifier = Modifier
            .fillMaxWidth()
            .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.5f))
            .border(2.dp, OnTimeColors.GoldDim)
            .clickable { showDate = true }
            .padding(OnTimeSpacing.md),
    )
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
            title = { Text("选择日期", color = OnTimeColors.InkWhite) },
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
            title = { Text("选择时间", color = OnTimeColors.InkWhite) },
            text = { TimePicker(state = tp) },
            containerColor = OnTimeColors.DeepBlueHigh,
        )
    }
}

@Composable
private fun WeekPicker(weekMask: Int, onToggle: (bit: Int, on: Boolean) -> Unit) {
    val names = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.xs)) {
        names.forEachIndexed { i, name ->
            val bit = 1 shl i
            val on = weekMask and bit != 0
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (on) OnTimeColors.DeepBlue else OnTimeColors.InkMuted,
                modifier = Modifier
                    .background(if (on) OnTimeColors.Gold else OnTimeColors.DeepBlueHigh.copy(alpha = 0.5f))
                    .border(2.dp, if (on) OnTimeColors.Gold else OnTimeColors.GoldDim)
                    .clickable { onToggle(bit, !on) }
                    .padding(OnTimeSpacing.sm),
            )
        }
    }
}

@Composable
private fun NumberField(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label  ", style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.InkMuted, modifier = Modifier.weight(1f))
        PixelButton(onClick = { onChange(value - if (value <= 15) 1 else 5) }) { Text("−", style = MaterialTheme.typography.titleMedium) }
        Text(
            " $value ",
            style = MaterialTheme.typography.titleLarge, color = OnTimeColors.InkWhite,
        )
        PixelButton(onClick = { onChange(value + if (value < 15) 1 else 5) }) { Text("+", style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
private fun CyclerField(label: String, text: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label", style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.InkMuted, modifier = Modifier.weight(1f))
        PixelButton(onClick = onPrev) { Text("◀", style = MaterialTheme.typography.bodyMedium) }
        Text(" $text ", style = MaterialTheme.typography.bodyLarge, color = OnTimeColors.Gold)
        PixelButton(onClick = onNext) { Text("▶", style = MaterialTheme.typography.bodyMedium) }
    }
}
