package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.data.ReminderEntity

object ReminderScheduler {
    const val ACTION_FIRE_REMINDER = "com.example.ACTION_FIRE_REMINDER"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"

    private fun buildPendingIntent(context: Context, reminderId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
            data = Uri.parse("noteall://reminder/$reminderId")
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildPendingIntent(context, reminder.id)

        if (!reminder.isEnabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val now = System.currentTimeMillis()
        val triggerAt = if (reminder.isDaily && reminder.triggerAtMillis <= now) {
            ReminderEntity.computeNextDailyTriggerMillis(reminder.hour, reminder.minute, now)
        } else {
            reminder.triggerAtMillis
        }

        if (!reminder.isDaily && triggerAt <= now) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val useExactIfPermitted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (useExactIfPermitted) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildPendingIntent(context, reminderId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun rescheduleAllEnabled(context: Context, reminders: List<ReminderEntity>) {
        reminders.filter { it.isEnabled }.forEach { reminder ->
            scheduleReminder(context, reminder)
        }
    }
}
