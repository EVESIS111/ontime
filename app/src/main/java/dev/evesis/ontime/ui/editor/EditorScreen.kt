package dev.evesis.ontime.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.evesis.ontime.ui.components.WorkspacePage
import dev.evesis.ontime.ui.components.WorkspacePanel
import dev.evesis.ontime.ui.components.WorkspaceColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.Sounds
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelTextField
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.components.SectionHeader
import dev.evesis.ontime.ui.theme.OnTimeBodyLarge
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeFormLabel
import dev.evesis.ontime.ui.theme.OnTimeSecondary
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 固定导航和保存区，表单按内容、时间、声音组织。
@Composable
fun EditorScreen(viewModel: EditorViewModel, id: Long, onDone: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var previewMsg by remember { mutableStateOf<String?>(null) }

    var advanced by rememberSaveable(id) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(id) { mutableStateOf(false) }

    LaunchedEffect(id) { viewModel.load(id, context) }
    DisposableEffect(Unit) { onDispose { PreviewPlayer.stop() } }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { PreviewPlayer.stop() }
    LaunchedEffect(state.finished) { if (state.finished) onDone() }

    WorkspacePage(if (state.id == 0L) "新建提醒" else "编辑提醒", "写下要做的事，安排时间与声音",
        modifier = Modifier.imePadding(), actions = { QuietButton(onClick = onDone, text = "取消") }) {
        WorkspaceColumns(Modifier.weight(1f), leading = {
            WorkspacePanel("提醒内容", "标题用于显示，台词用于到点播报") {
            FieldRow("标题") {
                PixelTextField(label = "", value = state.title, placeholder = "例如：喝水、出门、学英语",
                    onValueChange = { v -> viewModel.update { it.copy(title = v) } })
            }
            FieldRow("播报台词") {
                PixelTextField(label = "", value = state.message, minLines = 2, placeholder = "到点想听到的话，多句可用 | 分隔",
                    onValueChange = { v -> viewModel.update { it.copy(message = v) } })
            }

            }
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            WorkspacePanel("角色声音", "选择陪你记住这件事的声音") {
            FieldRow("音色") {
                OptionSelector(
                    value = if (state.voiceId.isEmpty()) "跟随系统" else dev.evesis.ontime.VoicePacks.displayName(state.voiceId),
                    options = state.availableVoices.map { it to if (it.isEmpty()) "跟随系统" else dev.evesis.ontime.VoicePacks.displayName(it) },
                    currentId = state.voiceId,
                    onPick = { value -> viewModel.update { it.copy(voiceId = value) } },
                ) { togglePreview(state, context, voice = true) { previewMsg = it } }
            }
            previewMsg?.let { Text(it, style = OnTimeSecondary, color = OnTimeColors.Gold) }
            }
        }, trailing = {
            WorkspacePanel("时间安排") {
            FieldRow("重复") { RepeatSelector(state.repeatType) { t ->
                viewModel.update {
                    val now = System.currentTimeMillis()
                    it.copy(repeatType = t, atMillis =
                        if (t == ScheduleEngine.RepeatType.ONCE && it.atMillis <= now) now + 60 * 60_000L
                        else it.atMillis)
                }
            } }
            when (state.repeatType) {
                ScheduleEngine.RepeatType.DAILY, ScheduleEngine.RepeatType.WEEKLY -> {
                    FieldRow("时间") { TimeStepper(state.timeOfDay) { m -> viewModel.update { it.copy(timeOfDay = m) } } }
                    if (state.repeatType == ScheduleEngine.RepeatType.WEEKLY) {
                        FieldRow("星期") { WeekSelector(state.weekMask) { bit, on ->
                            viewModel.update { it.copy(weekMask = if (on) it.weekMask or bit else it.weekMask and bit.inv()) }
                        } }
                    }
                }
                ScheduleEngine.RepeatType.INTERVAL -> {
                    FieldRow("间隔(分钟)") {
                        NumberEntry(state.intervalMinutes, 1..1440) { value -> viewModel.update { it.copy(intervalMinutes = value) } }
                    }
                    FieldRow("时间窗口") {
                        QuietButton(
                            onClick = {
                                viewModel.update {
                                    if (it.windowStart >= 0) it.copy(windowStart = -1, windowEnd = -1)
                                    else it.copy(windowStart = 9 * 60, windowEnd = 21 * 60 + 30)
                                }
                            },
                            text = if (state.windowStart >= 0)
                                "%02d:%02d - %02d:%02d ✓".format(
                                    state.windowStart / 60, state.windowStart % 60,
                                    state.windowEnd / 60, state.windowEnd % 60)
                            else "全天任意时段 · 点击限定时段",
                            emphasize = state.windowStart >= 0,
                        )
                        if (state.windowStart >= 0) {
                            Text("开始时间", style = OnTimeSecondary, color = OnTimeColors.InkMuted)
                            TimeStepper(state.windowStart) { value -> viewModel.update { it.copy(windowStart = value) } }
                            Text("结束时间", style = OnTimeSecondary, color = OnTimeColors.InkMuted)
                            TimeStepper(state.windowEnd) { value -> viewModel.update { it.copy(windowEnd = value) } }
                        }
                    }
                }
                ScheduleEngine.RepeatType.ONCE -> {
                    FieldRow("日期") {
                        QuietButton(onClick = {
                            val date = Calendar.getInstance().apply { timeInMillis = state.atMillis }
                            android.app.DatePickerDialog(context, dev.evesis.ontime.R.style.OnTimeDialog, { _, year, month, day ->
                                viewModel.update {
                                    val chosen = Calendar.getInstance().apply { timeInMillis = it.atMillis
                                        set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day) }
                                    it.copy(atMillis = chosen.timeInMillis)
                                }
                            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
                        }, text = SimpleDateFormat("yyyy年M月d日 · 选择日期", Locale.CHINA).format(Date(state.atMillis)))
                    }
                    FieldRow("时间") { TimeStepper(minutesFrom(state.atMillis)) { hm ->
                        viewModel.update {
                            val c = Calendar.getInstance().apply { timeInMillis = it.atMillis }
                            c.set(Calendar.HOUR_OF_DAY, hm / 60); c.set(Calendar.MINUTE, hm % 60); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
                            it.copy(atMillis = c.timeInMillis)
                        }
                    } }
                }
            }

            }
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            WorkspacePanel("其他选项", "音效、稍后提醒与删除") {
            QuietButton(onClick = { advanced = !advanced; confirmDelete = false },
                text = if (advanced) "收起更多设置 ▴" else "更多设置 ▾", modifier = Modifier.fillMaxWidth())
            if (advanced) {
            FieldRow("提醒前的音效") {
                OptionSelector(
                    value = Sounds.byId(state.soundId).label,
                    options = Sounds.ALL.map { it.id to it.label }, currentId = state.soundId,
                    onPick = { value -> viewModel.update { it.copy(soundId = value) } },
                ) { togglePreview(state, context, voice = false) { previewMsg = it } }
            }
            FieldRow("稍后提醒(分钟)") {
                NumberEntry(state.snoozeMinutes, 1..60) { value -> viewModel.update { it.copy(snoozeMinutes = value) } }
            }

            if (state.id > 0) {
                if (confirmDelete) {
                    Text("删除后无法撤销", style = OnTimeSecondary, color = OnTimeColors.Gold)
                    Row(Modifier.fillMaxWidth()) {
                        QuietButton(onClick = { confirmDelete = false }, text = "保留提醒", modifier = Modifier.weight(1f))
                        QuietButton(onClick = viewModel::delete, text = "确认删除", modifier = Modifier.weight(1f))
                    }
                } else QuietButton(onClick = { confirmDelete = true }, text = "删除这条提醒")
            }
            }
            }
        })
        Row(Modifier.fillMaxWidth().padding(top = OnTimeSpacing.lg), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.xl)) {
            Text(state.error ?: "保存后将按此计划提醒", style = OnTimeSecondary,
                color = if (state.error == null) OnTimeColors.InkMuted else OnTimeColors.Gold,
                modifier = Modifier.weight(1f))
            PixelButton(onClick = viewModel::save) {
                Text(if (state.id == 0L) "创建提醒" else "保存修改", style = OnTimeButtonLabel)
            }
        }
    }
}

private fun minutesFrom(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
}

private fun togglePreview(
    state: EditorUiState,
    context: android.content.Context,
    voice: Boolean,
    onMsg: (String?) -> Unit,
) {
    val text = state.message.split('|').firstOrNull()?.trim().orEmpty().ifBlank { state.title }
    if (voice) PreviewPlayer.toggleVoice(context, state.voiceId, text, onMsg)
    else PreviewPlayer.toggleSound(context, state.soundId, onMsg)
}

/** 标签与控件同一左边界。 */
@Composable
private fun FieldRow(label: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.compGap)) {
        if (label.isNotEmpty()) {
            Text(label, style = OnTimeFormLabel, color = OnTimeColors.InkWhite)
        }
        Column(Modifier.padding(top = if (label.isEmpty()) 0.dp else OnTimeSpacing.xs)) { content() }
    }
}

/** 时、分独立等宽分组，数字与各自操作对应。 */
@Composable
private fun TimeStepper(minutes: Int, onPick: (Int) -> Unit) {
    val context = LocalContext.current
    PixelButton(onClick = {
        android.app.TimePickerDialog(context, dev.evesis.ontime.R.style.OnTimeDialog, { _, hour, minute -> onPick(hour * 60 + minute) },
            minutes / 60, minutes % 60, true).show()
    }, modifier = Modifier.fillMaxWidth()) {
        Text("%02d:%02d  ·  选择时间".format(minutes / 60, minutes % 60), style = OnTimeBodyLarge)
    }
}

/** Numeric entry avoids repeated taps for interval and snooze duration. */
@Composable
private fun NumberEntry(value: Int, range: IntRange, onPick: (Int) -> Unit) {
    val context = LocalContext.current
    QuietButton(onClick = {
        val dialogContext = android.view.ContextThemeWrapper(context, dev.evesis.ontime.R.style.OnTimeDialog)
        val input = android.widget.EditText(dialogContext).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(value.toString()); selectAll()
        }
        val dialog = android.app.AlertDialog.Builder(dialogContext).setTitle("分钟数（${range.first}–${range.last}）")
            .setView(input).setNegativeButton("取消", null).setPositiveButton("确定", null).create()
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val number = input.text.toString().toIntOrNull()
                if (number == null || number !in range) input.error = "请输入 ${range.first}–${range.last}"
                else { onPick(number); dialog.dismiss() }
            }
        }
        dialog.show()
    }, text = "$value 分钟 · 修改", modifier = Modifier.fillMaxWidth(), emphasize = true)
}

/** 重复类型四选(统一 cell:minWidth+同高同 padding;选中金底) */
@Composable
private fun RepeatSelector(current: ScheduleEngine.RepeatType, onPick: (ScheduleEngine.RepeatType) -> Unit) {
    val labels = listOf(
        ScheduleEngine.RepeatType.DAILY to "每天",
        ScheduleEngine.RepeatType.WEEKLY to "每周",
        ScheduleEngine.RepeatType.INTERVAL to "间隔",
        ScheduleEngine.RepeatType.ONCE to "一次",
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm)) {
        labels.forEach { (t, label) ->
            val selected = current == t
            Box(Modifier.weight(1f).heightIn(min = 56.dp)
                .background(if (selected) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
                .clickable(interactionSource = null, indication = null) { onPick(t) }.padding(8.dp), contentAlignment = Alignment.Center) {
                Text(label, style = OnTimeButtonLabel,
                    color = if (selected) OnTimeColors.DeepBlue else OnTimeColors.Gold)
            }
        }
    }
}

/** 周选择(统一 cell;FlowRow 窄屏自动换行) */
@Composable
private fun WeekSelector(weekMask: Int, onToggle: (bit: Int, on: Boolean) -> Unit) {
    val names = listOf("日", "一", "二", "三", "四", "五", "六")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm),
    ) {
        names.forEachIndexed { i, name ->
            val bit = 1 shl i
            val on = weekMask and bit != 0
            Text(
                name,
                style = OnTimeButtonLabel,
                color = if (on) OnTimeColors.DeepBlue else OnTimeColors.InkMuted,
                modifier = Modifier
                    .background(if (on) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
                    .clickable(interactionSource = null, indication = null) { onToggle(bit, !on) }
                    .padding(horizontal = OnTimeSpacing.lg, vertical = OnTimeSpacing.md),
            )
        }
    }
}

/** 当前值居中，切换与试听分行。 */
@Composable
private fun OptionSelector(
    value: String,
    options: List<Pair<String, String>>,
    currentId: String,
    onPick: (String) -> Unit,
    onPreview: () -> Unit,
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth()) {
        QuietButton(onClick = {
            android.app.AlertDialog.Builder(context, dev.evesis.ontime.R.style.OnTimeDialog)
                .setTitle("选择声音")
                .setSingleChoiceItems(options.map { it.second }.toTypedArray(), options.indexOfFirst { it.first == currentId }) { dialog, index ->
                    PreviewPlayer.stop(); onPick(options[index].first); dialog.dismiss()
                }.setNegativeButton("取消", null).show()
        }, text = "$value  ·  更换", modifier = Modifier.fillMaxWidth(), emphasize = true)
        QuietButton(onClick = onPreview, text = "试听 / 停止", modifier = Modifier.fillMaxWidth())
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Preview(name = "Phone", widthDp = 360, heightDp = 720)
@Composable
private fun EditorPartsPreview() {
    OnTimeTheme {
        Column(Modifier.background(OnTimeColors.DeepBlue).padding(24.dp)) {
            SectionHeader("02  什么时候")
            RepeatSelector(ScheduleEngine.RepeatType.DAILY) { }
            WeekSelector(0b0111110) { _, _ -> }
        }
    }
}
