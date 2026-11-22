package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ReminderTargetTab
import com.example.ui.i18n.AppStrings

object ReminderNotificationHelper {
    const val CHANNEL_ID = "note_all_reminders_channel"
    const val ACTION_OPEN_REMINDER_TARGET = "com.example.ACTION_OPEN_REMINDER_TARGET"
    const val EXTRA_TARGET_TAB = "extra_target_tab"
    const val EXTRA_NOTE_ID = "extra_note_id"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun ensureNotificationChannel(
        context: Context,
        channelName: String = "Note All Reminders",
        channelDesc: String = "Scheduled reminders for notes, finance, and daily activities"
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttrs = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = channelDesc
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0L, 220L, 120L, 220L)
                    setSound(soundUri, audioAttrs)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun showReminderNotification(
        context: Context,
        reminderId: String,
        title: String,
        body: String,
        targetTab: String,
        linkedNoteId: String? = null
    ) {
        ensureNotificationChannel(context)
        if (!hasNotificationPermission(context)) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_REMINDER_TARGET
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, targetTab)
            if (!linkedNoteId.isNullOrBlank()) {
                putExtra(EXTRA_NOTE_ID, linkedNoteId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title.ifBlank { context.getString(R.string.app_name) })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
    }

    fun sendTestNotification(context: Context, strings: AppStrings) {
        showReminderNotification(
            context = context,
            reminderId = "test_notification",
            title = strings.testNotificationSampleTitle,
            body = strings.testNotificationSampleBody,
            targetTab = ReminderTargetTab.NOTES.key,
            linkedNoteId = null
        )
    }
}
