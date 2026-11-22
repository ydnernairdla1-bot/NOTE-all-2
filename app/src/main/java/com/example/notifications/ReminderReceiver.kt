package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.PreferencesStore
import com.example.data.ReminderEntity
import com.example.ui.i18n.stringsForLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dao = db.reminderDao()
                val reminder = dao.getReminderById(reminderId) ?: return@launch
                if (!reminder.isEnabled) return@launch

                val language = PreferencesStore(context).appearanceSettings.first().language
                val strings = stringsForLanguage(language)

                val displayTitle = when (reminder.id) {
                    ReminderEntity.PRESET_FINANCE_DAILY_ID -> strings.presetFinanceReminderTitle
                    ReminderEntity.PRESET_ACTIVITIES_DAILY_ID -> strings.presetActivitiesReminderTitle
                    else -> reminder.title
                }
                val displayBody = when (reminder.id) {
                    ReminderEntity.PRESET_FINANCE_DAILY_ID -> strings.presetFinanceReminderBody
                    ReminderEntity.PRESET_ACTIVITIES_DAILY_ID -> strings.presetActivitiesReminderBody
                    else -> reminder.message.ifBlank { strings.defaultReminderNotificationBody }
                }

                ReminderNotificationHelper.showReminderNotification(
                    context = context,
                    reminderId = reminder.id,
                    title = displayTitle,
                    body = displayBody,
                    targetTab = reminder.targetTab,
                    linkedNoteId = reminder.linkedNoteId
                )

                val now = System.currentTimeMillis()
                if (reminder.isDaily) {
                    val nextTrigger = ReminderEntity.computeNextDailyTriggerMillis(
                        hour = reminder.hour,
                        minute = reminder.minute,
                        nowMillis = now + 1_000L
                    )
                    val updated = reminder.copy(
                        triggerAtMillis = nextTrigger,
                        updatedAt = now
                    )
                    dao.upsertReminder(updated)
                    ReminderScheduler.scheduleReminder(context, updated)
                } else {
                    dao.upsertReminder(
                        reminder.copy(
                            isEnabled = false,
                            updatedAt = now
                        )
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
