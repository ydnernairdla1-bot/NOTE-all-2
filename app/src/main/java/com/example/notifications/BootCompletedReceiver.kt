package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).reminderDao()
                val enabled = dao.getEnabledRemindersSnapshot()
                val now = System.currentTimeMillis()
                enabled.forEach { reminder ->
                    if (reminder.isDaily) {
                        val nextTrigger = ReminderEntity.computeNextDailyTriggerMillis(
                            hour = reminder.hour,
                            minute = reminder.minute,
                            nowMillis = now
                        )
                        val updated = if (reminder.triggerAtMillis <= now) {
                            reminder.copy(triggerAtMillis = nextTrigger, updatedAt = now).also {
                                dao.upsertReminder(it)
                            }
                        } else {
                            reminder
                        }
                        ReminderScheduler.scheduleReminder(context, updated)
                    } else if (reminder.triggerAtMillis > now) {
                        ReminderScheduler.scheduleReminder(context, reminder)
                    } else {
                        dao.upsertReminder(reminder.copy(isEnabled = false, updatedAt = now))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
