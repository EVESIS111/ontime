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
import androidx.compose.foundation.layout.navigationBarsPadding
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

/*
 * Editor v4 — Layout Rewrite(v11.4 Stabilization;ViewModel/State/Callbacks 契约不变)
 * 规则(LAYOUT_INVARIANTS):单一布局(所有视口单列,横屏 maxwidth 720 居中——§35 简化优先);
 * 统一 FieldRow 节奏(label→control);PixelOptionSelector 唯一(prev/value/next/preview);
 * 每个字段恰好一处;保存恰一个;零手工 offset/scale。
 */

@Composable
fun EditorScreen(viewModel: EditorViewModel, id: Long, onDone: () -> Unit) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var previewMsg by remember { mutableStateOf<String?>(null) }

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
        Column(
            Modifier
                .widthIn(max = 720.dp)          // 先限宽再填充，否则 fillMaxSize 会锁死全屏宽度
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = OnTimeSpacing.xxl,
                    start = OnTimeSpacing.xl,
                    end = OnTimeSpacing.xl,
                    bottom = OnTimeSpacing.xxl,
                ),
        ) {
            // ── Top Bar(Back 左 / Delete 右,同高同基线)──
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuietButton(onClick = onDone, text = "← 取消")
                if (state.id > 0) QuietButton(onClick = viewModel::delete, text = "删除")
            }
            Text(
                if (state.id == 0L) "新提醒" else "编辑提醒",
                style = OnTimeScreenTitle, color = OnTimeColors.InkWhite,
                modifier = Modifier.padding(top = OnTimeSpacing.md),
            )

            // ── 内容 ──
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("内容")
            FieldRow("标题") {
                PixelTextField(label = "", value = state.title,
                    onValueChange = { v -> viewModel.update { it.copy(title = v) } })
            }
            FieldRow("台词(多句用 | 分隔)") {
                PixelTextField(label = "", value = state.message, minLines = 2,
                    onValueChange = { v -> viewModel.update { it.copy(message = v) } })
            }

            // ── 计划 ──
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("计划")
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
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("体验")
            FieldRow("音色") {
                OptionSelector(
                    value = if (state.voiceId.isEmpty()) "跟随系统" else dev.evesis.ontime.VoicePacks.displayName(state.voiceId),
                    onPrev = { viewModel.update { it.copy(voiceId = cycle(state.availableVoices, state.voiceId, -1)) } },
                    onNext = { viewModel.update { it.copy(voiceId = cycle(state.availableVoices, state.voiceId, +1)) } },
                ) { togglePreview(state, context, voice = true) { previewMsg = it } }
            }
            FieldRow("音效") {
                OptionSelector(
                    value = Sounds.byId(state.soundId).label,
                    onPrev = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, state.soundId, -1)) } },
                    onNext = { viewModel.update { it.copy(soundId = cycle(Sounds.ALL.map { s -> s.id }, state.soundId, +1)) } },
                ) { togglePreview(state, context, voice = false) { previewMsg = it } }
            }
            previewMsg?.let {
                Text(it, style = OnTimeSecondary, color = OnTimeColors.Gold,
                    modifier = Modifier.padding(top = OnTimeSpacing.xs))
            }

            // ── 行为 ──
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("行为")
            FieldRow("稍后提醒(分钟)") {
                ValueStepper(value = "${state.snoozeMinutes}",
                    big = false,
                    onPrev = { viewModel.update { it.copy(snoozeMinutes = (it.snoozeMinutes - if (it.snoozeMinutes <= 15) 1 else 5).coerceIn(1, 60)) } },
                    onNext = { viewModel.update { it.copy(snoozeMinutes = (it.snoozeMinutes + if (it.snoozeMinutes < 15) 1 else 5).coerceIn(1, 60)) } })
            }

            // ── 保存(唯一;safe bottom)──
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            state.error?.let {
                Text(it, style = OnTimeSecondary, color = OnTimeColors.Gold,
                    modifier = Modifier.padding(bottom = OnTimeSpacing.md))
            }
            PixelButton(onClick = viewModel::save, modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()) {
                Text("保存", style = OnTimeButtonLabel)
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

/** 统一字段节奏:Label(固定 Metadata 角色)→ Control;每字段间 compGap(§31 一致 Section Rhythm) */
@Composable
private fun FieldRow(label: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.compGap)) {
        if (label.isNotEmpty()) {
            Text(label, style = OnTimeMetadata, color = OnTimeColors.InkMuted)
        }
        Column(Modifier.padding(top = if (label.isEmpty()) 0.dp else OnTimeSpacing.xs)) { content() }
    }
}

/** 时间:值行(HH : mm 大字居中)+ 操作行(时组|分组各半宽)——结构上杜绝行内挤压重叠 */
@Composable
private fun TimeStepper(minutes: Int, onPick: (Int) -> Unit) {
    val h = minutes / 60
    val m = minutes % 60
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "%02d : %02d".format(h, m),
            style = OnTimeRowTime,
            color = OnTimeColors.Gold,
            modifier = Modifier.padding(vertical = OnTimeSpacing.xs),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.md)) {
            StepperGroup(Modifier.weight(1f), "时", onPrev = { onPick(((h + 23) % 24) * 60 + m) }, onNext = { onPick(((h + 1) % 24) * 60 + m) })
            StepperGroup(Modifier.weight(1f), "分", onPrev = { onPick(h * 60 + (m + 59) % 60) }, onNext = { onPick(h * 60 + (m + 1) % 60) })
        }
    }
}

/** 操作组:◀ 标签 ▶;weight(1f) 均分,组内 SpaceEvenly,永不超宽 */
@Composable
private fun StepperGroup(modifier: Modifier = Modifier, label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuietButton(onClick = onPrev, text = "◀", modifier = Modifier.weight(1f))
        Text(label, style = OnTimeMetadata, color = OnTimeColors.InkMuted)
        QuietButton(onClick = onNext, text = "▶", modifier = Modifier.weight(1f))
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
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm),
    ) {
        labels.forEach { (t, label) ->
            val selected = current == t
            Text(
                label,
                style = OnTimeButtonLabel,
                color = if (selected) OnTimeColors.DeepBlue else OnTimeColors.Gold,
                modifier = Modifier
                    .background(if (selected) OnTimeColors.Gold else OnTimeColors.InkWhite.copy(alpha = 0.05f))
                    .clickable { onPick(t) }
                    .padding(horizontal = OnTimeSpacing.xl, vertical = OnTimeSpacing.md),
            )
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

/** 唯一 OptionSelector(§24):值行 + [◀ ▶ 试听] 操作行;左右镜像 */
@Composable
private fun OptionSelector(
    value: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPreview: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(value, style = OnTimeBodyLarge, color = OnTimeColors.Gold)
        Row(
            Modifier.fillMaxWidth().padding(top = OnTimeSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuietButton(onClick = onPrev, text = "◀", modifier = Modifier.weight(1f))
            QuietButton(onClick = onNext, text = "▶", modifier = Modifier.weight(1f))
            QuietButton(onClick = onPreview, text = "试听", modifier = Modifier.weight(1f))
        }
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Preview(name = "Phone", widthDp = 360, heightDp = 720)
@Composable
private fun EditorPartsPreview() {
    OnTimeTheme {
        Column(Modifier.background(OnTimeColors.DeepBlue).padding(24.dp)) {
            SectionHeader("计划")
            RepeatSelector(ScheduleEngine.RepeatType.DAILY) { }
            WeekSelector(0b0111110) { _, _ -> }
        }
    }
}
