package com.example.mywallpaper.data.model

import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class NotificationItem(
    val notificationId: String = "",
    val type: String = "NEW_WALLPAPER",
    val title: String = "",
    val body: String = "",
    val wallpaperId: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val createdAt: Any? = null,
    val read: Boolean = false
) {
    fun getTimestampMillis(): Long {
        return when (createdAt) {
            is Timestamp -> createdAt.toDate().time
            is Long -> createdAt
            is Double -> createdAt.toLong()
            else -> System.currentTimeMillis()
        }
    }

    fun relativeTimeString(): String {
        val now = System.currentTimeMillis()
        val diff = (now - getTimestampMillis()).coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            days < 7 -> "${days}d ago"
            else -> {
                val cal = Calendar.getInstance().apply { timeInMillis = getTimestampMillis() }
                String.format(
                    java.util.Locale.getDefault(),
                    "%tb %td",
                    cal,
                    cal
                )
            }
        }
    }

    fun dateGroup(): String {
        val now = Calendar.getInstance()
        val notifCal = Calendar.getInstance().apply { timeInMillis = getTimestampMillis() }

        val isSameDay = now.get(Calendar.YEAR) == notifCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == notifCal.get(Calendar.DAY_OF_YEAR)
        if (isSameDay) return "Today"

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == notifCal.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == notifCal.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) return "Yesterday"

        val diffDays = TimeUnit.MILLISECONDS.toDays(now.timeInMillis - notifCal.timeInMillis)
        if (diffDays in 2..7) return "This Week"

        return "Earlier"
    }
}
