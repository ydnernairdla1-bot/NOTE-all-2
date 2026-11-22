package com.example.ui.utils

import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.indonesianStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRelativeTime(
    timestampMs: Long,
    strings: AppStrings = indonesianStrings(),
    locale: Locale = Locale("id", "ID")
): String {
    val diff = System.currentTimeMillis() - timestampMs
    if (diff < 60_000L) return strings.justNow
    val minutes = diff / 60_000L
    if (minutes < 60L) return strings.minutesAgo(minutes)
    val hours = diff / 3_600_000L
    if (hours < 24L) return strings.hoursAgo(hours)
    val days = diff / 86_400_000L
    if (days == 1L) return strings.yesterday
    val sdf = SimpleDateFormat("dd MMM yyyy", locale)
    return sdf.format(Date(timestampMs))
}

fun formatFullTimestamp(
    timestampMs: Long,
    locale: Locale = Locale("id", "ID")
): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)
    return sdf.format(Date(timestampMs))
}

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.2f MB", mb)
}
