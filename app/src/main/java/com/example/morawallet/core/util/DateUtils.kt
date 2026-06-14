package com.example.morawallet.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Date helpers using Calendar/SimpleDateFormat (safe for minSdk 24, no desugaring). */
object DateUtils {

    fun formatDate(millis: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))

    fun formatTime(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

    fun formatDateTime(millis: Long): String =
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))

    fun startOfDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun endOfDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = startOfDay(millis)
            add(Calendar.DAY_OF_YEAR, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis

    fun isFutureDay(millis: Long, now: Long = System.currentTimeMillis()): Boolean =
        startOfDay(millis) > startOfDay(now)

    fun withDatePreservingTime(dateMillis: Long, currentMillis: Long): Long {
        val current = Calendar.getInstance().apply { timeInMillis = currentMillis }
        return Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, current.get(Calendar.MINUTE))
            set(Calendar.SECOND, current.get(Calendar.SECOND))
            set(Calendar.MILLISECOND, current.get(Calendar.MILLISECOND))
        }.timeInMillis
    }

    fun withTime(dateMillis: Long, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun hourOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

    fun minuteOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.MINUTE)

    fun timeMinutes(millis: Long): Int = hourOf(millis) * 60 + minuteOf(millis)

    /** Stable key for grouping transactions by calendar day. */
    fun dayKey(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    /** "Today" / "Yesterday" / formatted date for day-group headers. */
    fun dayHeader(millis: Long): String {
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        val now = Calendar.getInstance()
        fun sameDay(a: Calendar, b: Calendar) =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

        if (sameDay(target, now)) return "Today"
        now.add(Calendar.DAY_OF_YEAR, -1)
        if (sameDay(target, now)) return "Yesterday"
        return formatDate(millis)
    }
}
