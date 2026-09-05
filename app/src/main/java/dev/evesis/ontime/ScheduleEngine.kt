package dev.evesis.ontime

import java.util.Calendar
import java.util.TimeZone

object ScheduleEngine {

    enum class RepeatType { DAILY, WEEKLY, INTERVAL, ONCE }

    data class Rule(
        val type: RepeatType,
        val timeOfDay: Int = 0,
        val weekMask: Int = 0,
        val intervalMinutes: Int = 60,
        val atMillis: Long = 0L,
        val windowStart: Int = -1,
        val windowEnd: Int = -1,
    )

    fun next(rule: Rule, from: Long, tz: TimeZone = TimeZone.getDefault()): Long? {
        return when (rule.type) {
            RepeatType.ONCE -> rule.atMillis.takeIf { it > from }

            RepeatType.DAILY -> {
                val cal = atTime(from, rule.timeOfDay, tz)
                if (cal.timeInMillis <= from) cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }

            RepeatType.WEEKLY -> {
                for (d in 0..8) {
                    val day = atTime(from, rule.timeOfDay, tz)
                    day.add(Calendar.DAY_OF_YEAR, d)
                    val bit = 1 shl (day.get(Calendar.DAY_OF_WEEK) - 1)
                    if (rule.weekMask and bit != 0 && day.timeInMillis > from) {
                        return day.timeInMillis
                    }
                }
                null
            }

            RepeatType.INTERVAL -> {
                val iv = rule.intervalMinutes.coerceAtLeast(1)
                if (rule.windowStart in 0..1439 && rule.windowEnd in (rule.windowStart + 1)..1439) {
                    nextWindowed(rule, iv, from, tz)
                } else {
                    val fromMin = from / 60000L // 对齐到分钟(向下取整)
                    (fromMin / iv + 1) * iv * 60000L
                }
            }
        }
    }

    private fun nextWindowed(rule: Rule, iv: Int, from: Long, tz: TimeZone): Long {
        var anchor = atTime(from, rule.windowStart, tz)
        var t = anchor.timeInMillis
        while (t <= from) t += iv * 60000L
        val dayEnd = atTime(from, rule.windowEnd, tz).timeInMillis
        if (t > dayEnd) {
            anchor.add(Calendar.DAY_OF_YEAR, 1)
            t = anchor.timeInMillis
        }
        return t
    }

    private fun atTime(from: Long, minutesOfDay: Int, tz: TimeZone): Calendar {
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = from
        cal.set(Calendar.HOUR_OF_DAY, minutesOfDay / 60)
        cal.set(Calendar.MINUTE, minutesOfDay % 60)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }


    const val QUIET_START_MIN = 0          // 00:00
    const val QUIET_END_MIN = 8 * 60 + 30  // 08:30

    fun inQuietWindow(minuteOfDay: Int, start: Int = QUIET_START_MIN,
                      end: Int = QUIET_END_MIN): Boolean =
        if (start <= end) minuteOfDay >= start && minuteOfDay < end
        else minuteOfDay >= start || minuteOfDay < end

    fun inQuietHours(now: Long, tz: TimeZone = TimeZone.getDefault()): Boolean {
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = now
        return inQuietWindow(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE))
    }

    fun quietEnd(now: Long, tz: TimeZone = TimeZone.getDefault()): Long {
        val cal = atTime(now, QUIET_END_MIN, tz)
        if (!inQuietHours(now, tz)) return now   // 不在窗口内,无需顺延
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }
}
