package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.TimeZone

enum class ReminderTargetTab(val key: String) {
    NOTES("NOTES"),
    FINANCE("FINANCE"),
    ACTIVITIES("ACTIVITIES");

    companion object {
        fun fromKey(key: String): ReminderTargetTab =
            entries.firstOrNull { it.key == key } ?: NOTES
    }
}

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String = "",
    val targetTab: String = ReminderTargetTab.NOTES.key,
    val linkedNoteId: String? = null,
    val hour: Int = 8,
    val minute: Int = 0,
    val isDaily: Boolean = false,
    val triggerAtMillis: Long,
    val isEnabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
) {
    val targetTabEnum: ReminderTargetTab
        get() = ReminderTargetTab.fromKey(targetTab)

    val isRoutinePreset: Boolean
        get() = id == PRESET_FINANCE_DAILY_ID || id == PRESET_ACTIVITIES_DAILY_ID

    fun formattedTime(): String = String.format("%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    companion object {
        const val PRESET_FINANCE_DAILY_ID = "preset_finance_daily"
        const val PRESET_ACTIVITIES_DAILY_ID = "preset_activities_daily"

        fun computeNextDailyTriggerMillis(
            hour: Int,
            minute: Int,
            nowMillis: Long = System.currentTimeMillis()
        ): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                set(Calendar.MINUTE, minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= nowMillis) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        fun combineUtcDateAndLocalTime(
            utcDateMillis: Long,
            hour: Int,
            minute: Int
        ): Long {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = utcDateMillis
            }
            val localCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                set(Calendar.MINUTE, minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return localCal.timeInMillis
        }

        fun todayUtcMidnightMillis(nowMillis: Long = System.currentTimeMillis()): Long {
            val localCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
            }
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(Calendar.YEAR, localCal.get(Calendar.YEAR))
                set(Calendar.MONTH, localCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH))
            }
            return utcCal.timeInMillis
        }
    }
}
