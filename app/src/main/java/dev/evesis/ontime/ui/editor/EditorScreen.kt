package dev.evesis.ontime.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.evesis.ontime.ui.theme.OnTimeLayout
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeRowTime
import dev.evesis.ontime.ui.theme.OnTimeScreenTitle
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

    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .safeDrawingPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = OnTimeLayout.editorMaxWidth).fillMaxSize()
            .padding(horizontal = OnTimeSpacing.xl)) {
            Row(Modifier.fillMaxWidth().padding(vertical = OnTimeSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (state.id == 0L) "新建提醒" else "编辑提醒",
                    style = OnTimeScreenTitle, color = OnTimeColors.InkWhite,
                    modifier = Modifier.weight(1f))
                QuietButton(onClick = onDone, text = "取消")
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            // ── 内容 ──
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            SectionHeader("01  提醒什么")
            FieldRow("标题") {
                PixelTextField(label = "", value = state.title, placeholder = "例如：喝水、出门、学英语",
                    onValueChange = { v -> viewModel.update { it.copy(title = v) } })
            }
            FieldRow("播报台词") {
                PixelTextField(label = "", value = state.message, minLines = 2, placeholder = "到点想听到的话，多句可用 | 分隔",
                    onValueChange = { v -> viewModel.update { it.copy(message = v) } })
            }

            // ── 计划 ──
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            SectionHeader("02  什么时候")
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
                        ValueStepper(value = "${state.intervalMinutes}",
                            onPrev = { viewModel.update { it.copy(intervalMinutes = (it.intervalMinutes - if (it.intervalMinutes <= 15) 1 else 5).coerceIn(1, 1440)) } },
                            onNext = { viewModel.update { it.copy(intervalMinutes = (it.intervalMinutes + if (it.intervalMinutes < 15) 1 else 5).coerceIn(1, 1440)) } })
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
                            else "全天任意时段",
                            emphasize = state.windowStart >= 0,
                        )
                    }
                }
                ScheduleEngine.RepeatType.ONCE -> {
                    FieldRow("日期") {
                        ValueStepper(
                            value = SimpleDateFormat("M月d日", Locale.CHINA).format(Date(state.atMillis)),
                            big = false,
                            onPrev = { viewModel.update { it.copy(atMillis = it.atMillis - 86_400_000L) } },
                            onNext = { viewModel.update { it.copy(atMillis = it.atMillis + 86_400_000L) } })
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

            // ── 体验(唯一 OptionSelector;试听内联)──
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            SectionHeader("03  用什么声音")
            FieldRow("音色") {
                OptionSelector(
                    value = if (state.voiceId.isEmpty()) "跟随系统" else dev.evesis.ontime.VoicePacks.displayName(state.voiceId),
                    onPrev = { viewModel.update { it.copy(voiceId = cycle(state.availableVoices, state.voiceId, -1)) } },
                    onNext = { viewModel.update { it.copy(voiceId = cycle(state.availableVoices, state.voiceId, +1)) } },
                ) { togglePreview(state, context, voice = true) { previewMsg = it } }
            }
            previewMsg?.let { Text(it, style = OnTimeSecondary, color = OnTimeColors.Gold) }
            QuietButton(onClick = { advanced = !advanced; confirmDelete = false },
                text = if (advanced) "收起更多设置 ▴" else "更多设置 ▾", modifier = Modifier.fillMaxWidth())
            if (advanced) {
            FieldRow("提醒前的音效") {
                OptionSelector(
                    value = Sounds.byId(state.soundId).label,
                    onPrev = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, state.soundId, -1)) } },
                    onNext = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, state.soundId, +1)) } },
                ) { togglePreview(state, context, voice = false) { previewMsg = it } }
            }
            FieldRow("稍后提醒(分钟)") {
                ValueStepper(value = "${state.snoozeMinutes}",
                    big = false,
                    onPrev = { viewModel.update { it.copy(snoozeMinutes = (it.snoozeMinutes - if (it.snoozeMinutes <= 15) 1 else 5).coerceIn(1, 60)) } },
                    onNext = { viewModel.update { it.copy(snoozeMinutes = (it.snoozeMinutes + if (it.snoozeMinutes < 15) 1 else 5).coerceIn(1, 60)) } })
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
            Spacer(Modifier.padding(bottom = OnTimeSpacing.xl))
            }
            Column(Modifier.fillMaxWidth().padding(vertical = OnTimeSpacing.md)) {
            state.error?.let {
                Text(it, style = OnTimeSecondary, color = OnTimeColors.Gold,
                    modifier = Modifier.padding(bottom = OnTimeSpacing.md))
            }
            PixelButton(onClick = viewModel::save, modifier = Modifier
                .fillMaxWidth()) {
                Text(if (state.id == 0L) "创建提醒" else "保存修改", style = OnTimeButtonLabel)
            }
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

private fun <T> cycle(list: List<T>, current: T, dir: Int): T {
    if (list.isEmpty()) return current
    val i = list.indexOf(current).let { if (it < 0) 0 else it }
    return list[Math.floorMod(i + dir, list.size)]
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
    val h = minutes / 60
    val m = minutes % 60
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.md)) {
        StepperGroup(Modifier.weight(1f), "%02d 时".format(h),
            onPrev = { onPick(((h + 23) % 24) * 60 + m) }, onNext = { onPick(((h + 1) % 24) * 60 + m) })
        StepperGroup(Modifier.weight(1f), "%02d 分".format(m),
            onPrev = { onPick(h * 60 + (m + 59) % 60) }, onNext = { onPick(h * 60 + (m + 1) % 60) })
    }
}

@Composable
private fun StepperGroup(modifier: Modifier = Modifier, label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Column(modifier.background(OnTimeColors.InkWhite.copy(alpha = 0.05f)), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = OnTimeBodyLarge, color = OnTimeColors.Gold,
            modifier = Modifier.padding(top = OnTimeSpacing.sm))
        Row(Modifier.fillMaxWidth()) {
            QuietButton(onClick = onPrev, text = "−", modifier = Modifier.weight(1f))
            QuietButton(onClick = onNext, text = "+", modifier = Modifier.weight(1f))
        }
    }
}

/** 数字:值行(大/中字居中)+ 操作行(◀ ▶ 均分) */
@Composable
private fun ValueStepper(value: String, big: Boolean = true, onPrev: () -> Unit, onNext: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = if (big) OnTimeRowTime else OnTimeBodyLarge,
            color = if (big) OnTimeColors.Gold else OnTimeColors.InkWhite,
            modifier = Modifier.padding(vertical = OnTimeSpacing.xs),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.md)) {
            QuietButton(onClick = onPrev, text = "◀", modifier = Modifier.weight(1f))
            QuietButton(onClick = onNext, text = "▶", modifier = Modifier.weight(1f))
        }
    }
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
                .clickable { onPick(t) }.padding(8.dp), contentAlignment = Alignment.Center) {
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
                    .clickable { onToggle(bit, !on) }
                    .padding(horizontal = OnTimeSpacing.lg, vertical = OnTimeSpacing.md),
            )
        }
    }
}

/** 当前值居中，切换与试听分行。 */
@Composable
private fun OptionSelector(
    value: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPreview: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().background(OnTimeColors.InkWhite.copy(alpha = 0.05f)),
            verticalAlignment = Alignment.CenterVertically) {
            QuietButton(onClick = onPrev, text = "◀")
            Text(value, style = OnTimeBodyLarge, color = OnTimeColors.Gold,
                modifier = Modifier.weight(1f).padding(OnTimeSpacing.sm),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            QuietButton(onClick = onNext, text = "▶")
        }
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
