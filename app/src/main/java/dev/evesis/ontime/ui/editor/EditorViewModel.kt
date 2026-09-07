package dev.evesis.ontime.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.ScheduleEngine
import dev.evesis.ontime.VoicePacks
import dev.evesis.ontime.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditorUiState(
    val id: Long = 0,                              // 0 = 新建
    val title: String = "",
    val message: String = "",
    val audioUri: String? = null,
    val lastFiredAt: Long = 0,
    val error: String? = null,
    val repeatType: ScheduleEngine.RepeatType = ScheduleEngine.RepeatType.DAILY,
    val timeOfDay: Int = 9 * 60,
    val weekMask: Int = 0b0111110,                 // 默认周一~五
    val intervalMinutes: Int = 45,
    val windowStart: Int = -1,                     // -1 = 无窗口
    val windowEnd: Int = -1,
    val atMillis: Long = 0,                        // ONCE
    val snoozeMinutes: Int = 5,
    val voiceId: String = "",
    val soundId: String = "coin",
    val enabled: Boolean = true,
    val availableVoices: List<String> = emptyList(),
    val finished: Boolean = false,                 // save/delete 完成后置位,UI 弹回
    val loadedId: Long = Long.MIN_VALUE,           // 幂等守卫:旋转重组不重载(§67 连续性)
)

class EditorViewModel(private val repo: ReminderRepository) : ViewModel() {

    private val _ui = MutableStateFlow(EditorUiState())
    val ui: StateFlow<EditorUiState> = _ui.asStateFlow()

    fun load(id: Long, ctx: Context) {
        if (_ui.value.loadedId == id) return       // 旋转/重组不覆盖未保存编辑
        val voices = listOf("") + (VoicePacks.dir(ctx).listFiles()?.filter { it.isDirectory }?.map { it.name }?.sorted() ?: emptyList())
        val r = if (id > 0) repo.find(id) else null
        if (id > 0 && r == null) {
            _ui.value = EditorUiState(loadedId = id, finished = true)
            return
        }
        _ui.value = if (r == null) EditorUiState(
            availableVoices = voices,
            atMillis = System.currentTimeMillis() + 60 * 60_000L,
            loadedId = id,
        ) else EditorUiState(
            id = r.id, title = r.title, message = r.message,
            audioUri = r.audioUri, lastFiredAt = r.lastFiredAt,
            repeatType = r.repeatType, timeOfDay = r.timeOfDay, weekMask = r.weekMask,
            intervalMinutes = r.intervalMinutes, windowStart = r.windowStart, windowEnd = r.windowEnd,
            atMillis = r.atMillis, snoozeMinutes = r.snoozeMinutes,
            voiceId = r.voiceId, soundId = r.soundId, enabled = r.enabled,
            availableVoices = if (voices.contains(r.voiceId) || r.voiceId.isEmpty()) voices
            else voices + r.voiceId,                 // TTS id(如 zh-CN-YunyangNeural)兜底显示
            loadedId = id,
        )
    }

    fun update(transform: (EditorUiState) -> EditorUiState) = _ui.update { transform(it).copy(error = null) }

    fun save() {
        val s = _ui.value
        if (s.finished) return
        val error = s.validationError(System.currentTimeMillis())
        if (error != null) { _ui.update { it.copy(error = error) }; return }
        val r = Reminder(
            id = s.id, title = s.title.trim(), message = s.message.ifBlank { s.title },
            audioUri = s.audioUri, voiceId = s.voiceId, soundId = s.soundId,
            repeatType = s.repeatType, timeOfDay = s.timeOfDay, weekMask = s.weekMask,
            intervalMinutes = s.intervalMinutes, atMillis = s.atMillis,
            snoozeMinutes = s.snoozeMinutes, enabled = s.enabled,
            nextFireAt = 0, lastFiredAt = s.lastFiredAt,
            windowStart = s.windowStart, windowEnd = s.windowEnd,
        )
        if (s.id == 0L) repo.insert(r) else repo.update(r)
        _ui.update { it.copy(finished = true) }
    }

    fun delete() {
        val s = _ui.value
        if (s.id > 0) repo.find(s.id)?.let { repo.delete(it) }
        _ui.update { it.copy(finished = true) }
    }
}

internal fun EditorUiState.validationError(now: Long): String? = when {
    title.isBlank() -> "请填写提醒标题"
    repeatType == ScheduleEngine.RepeatType.WEEKLY && weekMask == 0 -> "请至少选择一天"
    repeatType == ScheduleEngine.RepeatType.ONCE && atMillis <= now -> "请选择未来的提醒时间"
    repeatType == ScheduleEngine.RepeatType.INTERVAL && intervalMinutes <= 0 -> "提醒间隔必须大于零"
    else -> null
}
