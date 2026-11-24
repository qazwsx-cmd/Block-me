package com.blockme.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * App-wide Kotlin extension functions.
 * SPDX-License-Identifier: MIT
 */

// DataStore singleton per Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Format hour (0-23) and minute (0-59) using Chinese AM/PM labels.
 */
fun formatTimeWithAmPm(hourOfDay: Int, minuteOfHour: Int): String {
    val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
    val period = if (hourOfDay < 12) "上午" else "下午"
    return String.format(Locale.getDefault(), "%s %02d:%02d", period, hour12, minuteOfHour)
}

/**
 * Format milliseconds to HH:MM:SS string.
 */
fun Long.toFormattedTime(): String {
    if (this <= 0L) return "00:00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Format milliseconds to a concise Chinese duration string.
 */
fun Long.toShortDurationString(): String {
    if (this <= 0L) return "0 分钟"
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours} 小时 ${minutes} 分钟"
        hours > 0 -> "${hours} 小时"
        else -> "${minutes} 分钟"
    }
}

/**
 * Format milliseconds to fractional hours in Chinese.
 */
fun Long.toHoursString(): String {
    val hours = this / (1000.0 * 60 * 60)
    return if (hours < 1.0) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
        "${minutes} 分钟"
    } else {
        String.format(Locale.CHINA, "%.1f 小时", hours)
    }
}

/**
 * Clamp a Long value between min and max.
 */
fun Long.clamp(min: Long, max: Long): Long = maxOf(min, minOf(max, this))

/**
 * Convert minutes to milliseconds.
 */
fun Int.minutesToMs(): Long = this * 60 * 1000L

/**
 * Convert seconds to milliseconds.
 */
fun Int.secondsToMs(): Long = this * 1000L
