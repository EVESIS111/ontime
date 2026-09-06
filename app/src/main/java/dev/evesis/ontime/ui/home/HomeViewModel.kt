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
}
