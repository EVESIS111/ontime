package dev.evesis.ontime.ui.home

import androidx.lifecycle.ViewModel
import dev.evesis.ontime.Reminder
import dev.evesis.ontime.data.AlarmHealth
import dev.evesis.ontime.data.AlarmHealthProbe
import dev.evesis.ontime.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val reminders: List<Reminder> = emptyList(),
    val alarmHealth: AlarmHealth = AlarmHealth.HEALTHY,
    val selecting: Boolean = false,                 // 长按进入的多选模式
    val selectedIds: Set<Long> = emptySet(),
)

class HomeViewModel(private val repo: ReminderRepository) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init { refresh() }

    fun refresh() = _ui.update {
        it.copy(reminders = repo.list(), alarmHealth = repo.alarmHealth())
    }

    fun setEnabled(id: Long, on: Boolean) {
        repo.setEnabled(id, on)
        refresh()
    }

    /** 长按进入选择模式并选中首项 */
    fun beginSelect(id: Long) = _ui.update {
        it.copy(selecting = true, selectedIds = setOf(id))
    }

    fun toggleSelect(id: Long) = _ui.update {
        if (!it.selecting) it
        else {
            val next = if (id in it.selectedIds) it.selectedIds - id else it.selectedIds + id
            if (next.isEmpty()) it.copy(selecting = false, selectedIds = next)   // 全取消即退出
            else it.copy(selectedIds = next)
        }
    }

    fun deleteOne(id: Long) {
        _ui.value.reminders.find { it.id == id }?.let { repo.delete(it) }
        refresh()
    }

    fun clearSelection() = _ui.update { it.copy(selecting = false, selectedIds = emptySet()) }

    /** 批量删除(Repository.delete 已含闹钟注销) */
    fun deleteSelected() {
        val ids = _ui.value.selectedIds
        _ui.value.reminders.filter { it.id in ids }.forEach { repo.delete(it) }
        clearSelection()
        refresh()
    }
}
