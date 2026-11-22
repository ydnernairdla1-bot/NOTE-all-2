package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityEntity
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import com.example.data.ReminderEntity
import com.example.data.ReminderTargetTab
import com.example.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class RemindersUiState(
    val financeDailyReminder: ReminderEntity = defaultFinanceDaily(),
    val activitiesDailyReminder: ReminderEntity = defaultActivitiesDaily(),
    val customReminders: List<ReminderEntity> = emptyList(),
    val activeCount: Int = 0,
    val recentNotes: List<NoteEntity> = emptyList(),
    val recentActivities: List<ActivityEntity> = emptyList(),
    val statusMessage: String? = null
) {
    companion object {
        fun defaultFinanceDaily(now: Long = System.currentTimeMillis()): ReminderEntity =
            ReminderEntity(
                id = ReminderEntity.PRESET_FINANCE_DAILY_ID,
                title = "Catat Keuangan Hari Ini",
                message = "",
                targetTab = ReminderTargetTab.FINANCE.key,
                linkedNoteId = null,
                hour = 20,
                minute = 0,
                isDaily = true,
                triggerAtMillis = ReminderEntity.computeNextDailyTriggerMillis(20, 0, now),
                isEnabled = false,
                createdAt = now,
                updatedAt = now
            )

        fun defaultActivitiesDaily(now: Long = System.currentTimeMillis()): ReminderEntity =
            ReminderEntity(
                id = ReminderEntity.PRESET_ACTIVITIES_DAILY_ID,
                title = "Cek Aktivitas Harian",
                message = "",
                targetTab = ReminderTargetTab.ACTIVITIES.key,
                linkedNoteId = null,
                hour = 8,
                minute = 0,
                isDaily = true,
                triggerAtMillis = ReminderEntity.computeNextDailyTriggerMillis(8, 0, now),
                isEnabled = false,
                createdAt = now,
                updatedAt = now
            )
    }
}

class RemindersViewModel(
    private val appContext: Context,
    private val repository: NoteRepository
) : ViewModel() {

    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RemindersUiState> = combine(
        repository.allReminders,
        repository.allNotes,
        repository.allActivities,
        statusMessage
    ) { reminders, notes, activities, status ->
        val financePreset = reminders.firstOrNull { it.id == ReminderEntity.PRESET_FINANCE_DAILY_ID }
            ?: RemindersUiState.defaultFinanceDaily()
        val activitiesPreset = reminders.firstOrNull { it.id == ReminderEntity.PRESET_ACTIVITIES_DAILY_ID }
            ?: RemindersUiState.defaultActivitiesDaily()
        val custom = reminders.filterNot { it.isRoutinePreset }
        val activeCount = reminders.count { it.isEnabled }
        RemindersUiState(
            financeDailyReminder = financePreset,
            activitiesDailyReminder = activitiesPreset,
            customReminders = custom,
            activeCount = activeCount,
            recentNotes = notes.filter { it.title.isNotBlank() || it.content.isNotBlank() }.take(6),
            recentActivities = activities.take(6),
            statusMessage = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RemindersUiState()
    )

    fun toggleRoutinePreset(presetId: String, enabled: Boolean) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = repository.getReminderById(presetId) ?: when (presetId) {
                ReminderEntity.PRESET_FINANCE_DAILY_ID -> RemindersUiState.defaultFinanceDaily(now)
                else -> RemindersUiState.defaultActivitiesDaily(now)
            }
            val nextTrigger = ReminderEntity.computeNextDailyTriggerMillis(
                hour = current.hour,
                minute = current.minute,
                nowMillis = now
            )
            val updated = current.copy(
                isEnabled = enabled,
                triggerAtMillis = nextTrigger,
                updatedAt = now
            )
            repository.upsertReminder(updated)
            if (enabled) {
                ReminderScheduler.scheduleReminder(appContext, updated)
            } else {
                ReminderScheduler.cancelReminder(appContext, updated.id)
            }
        }
    }

    fun updateRoutinePresetTime(presetId: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = repository.getReminderById(presetId) ?: when (presetId) {
                ReminderEntity.PRESET_FINANCE_DAILY_ID -> RemindersUiState.defaultFinanceDaily(now)
                else -> RemindersUiState.defaultActivitiesDaily(now)
            }
            val nextTrigger = ReminderEntity.computeNextDailyTriggerMillis(hour, minute, now)
            val updated = current.copy(
                hour = hour,
                minute = minute,
                triggerAtMillis = nextTrigger,
                isEnabled = true,
                updatedAt = now
            )
            repository.upsertReminder(updated)
            ReminderScheduler.scheduleReminder(appContext, updated)
        }
    }

    fun saveCustomReminder(
        existingId: String?,
        title: String,
        message: String,
        targetTab: ReminderTargetTab,
        linkedNoteId: String?,
        isDaily: Boolean,
        dateUtcMillis: Long,
        hour: Int,
        minute: Int,
        savedMessageText: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = existingId?.let { repository.getReminderById(it) }
            val triggerAt = if (isDaily) {
                ReminderEntity.computeNextDailyTriggerMillis(hour, minute, now)
            } else {
                ReminderEntity.combineUtcDateAndLocalTime(dateUtcMillis, hour, minute)
            }
            val entity = ReminderEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                message = message.trim(),
                targetTab = targetTab.key,
                linkedNoteId = linkedNoteId,
                hour = hour,
                minute = minute,
                isDaily = isDaily,
                triggerAtMillis = triggerAt,
                isEnabled = true,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
            repository.upsertReminder(entity)
            ReminderScheduler.scheduleReminder(appContext, entity)
            statusMessage.value = savedMessageText
        }
    }

    fun toggleCustomReminder(reminder: ReminderEntity, enabled: Boolean) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val nextTrigger = if (!enabled) {
                reminder.triggerAtMillis
            } else if (reminder.isDaily) {
                ReminderEntity.computeNextDailyTriggerMillis(reminder.hour, reminder.minute, now)
            } else if (reminder.triggerAtMillis <= now) {
                // If re-enabling a past one-time reminder, schedule for the next occurrence of that hour:minute
                ReminderEntity.computeNextDailyTriggerMillis(reminder.hour, reminder.minute, now)
            } else {
                reminder.triggerAtMillis
            }
            val updated = reminder.copy(
                isEnabled = enabled,
                triggerAtMillis = nextTrigger,
                updatedAt = now
            )
            repository.upsertReminder(updated)
            if (enabled) {
                ReminderScheduler.scheduleReminder(appContext, updated)
            } else {
                ReminderScheduler.cancelReminder(appContext, updated.id)
            }
        }
    }

    fun deleteReminder(reminderId: String, deletedMessageText: String) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(appContext, reminderId)
            repository.deleteReminderById(reminderId)
            statusMessage.value = deletedMessageText
        }
    }

    fun setStatusMessage(message: String) {
        statusMessage.value = message
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    companion object {
        fun factory(
            appContext: Context,
            repository: NoteRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RemindersViewModel(appContext.applicationContext, repository) as T
                }
            }
    }
}
