package dev.evesis.ontime

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 提醒核心模型(自 v10.3 保留;PHASE8 起原地升级为 Room Entity,字段/列名/默认值
 * 与历史 SQLiteOpenHelper v3 schema 逐列一致——旧库直接打开,无需 Migration)。
 * 纯数据 + 纯方法,无 Android API → Multiplatform SHARED READY。
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "audio_uri") val audioUri: String?,      // 自定义音乐 content uri,优先级最高
    @ColumnInfo(name = "voice_id", defaultValue = "''") val voiceId: String,          // "" = 跟随系统 TTS
    @ColumnInfo(name = "sound_id", defaultValue = "'coin'") val soundId: String,      // 游戏提示音(Sounds.ALL)
    @ColumnInfo(name = "repeat_type") val repeatType: ScheduleEngine.RepeatType,
    @ColumnInfo(name = "time_of_day", defaultValue = "0") val timeOfDay: Int,         // 当日分钟数(INTERVAL 时作为循环开始)
    @ColumnInfo(name = "week_mask", defaultValue = "0") val weekMask: Int,            // bit(dow-1),周日=1
    @ColumnInfo(name = "interval_minutes", defaultValue = "60") val intervalMinutes: Int,
    @ColumnInfo(name = "at_millis", defaultValue = "0") val atMillis: Long,
    @ColumnInfo(name = "snooze_minutes", defaultValue = "10") val snoozeMinutes: Int,
    @ColumnInfo(name = "enabled", defaultValue = "1") val enabled: Boolean,
    @ColumnInfo(name = "next_fire_at", defaultValue = "0") val nextFireAt: Long,
    @ColumnInfo(name = "last_fired_at", defaultValue = "0") val lastFiredAt: Long,
    @ColumnInfo(name = "window_start", defaultValue = "-1") val windowStart: Int = -1,   // INTERVAL 日窗口开始,-1 = 无
    @ColumnInfo(name = "window_end", defaultValue = "-1") val windowEnd: Int = -1,
) {
    fun rule() = ScheduleEngine.Rule(repeatType, timeOfDay, weekMask, intervalMinutes, atMillis,
        windowStart, windowEnd)

    fun pickMessage(): String =
        message.split('|').map { it.trim() }.filter { it.isNotEmpty() }.let {
            if (it.size <= 1) message else it[kotlin.random.Random.nextInt(it.size)]
        }

    fun desc(): String {
        fun hhmm(m: Int) = "%02d:%02d".format(m / 60, m % 60)
        return when (repeatType) {
            ScheduleEngine.RepeatType.DAILY -> "每天 ${hhmm(timeOfDay)}"
            ScheduleEngine.RepeatType.WEEKLY -> {
                val names = listOf("日", "一", "二", "三", "四", "五", "六")
                val days = (0..6).filter { weekMask and (1 shl it) != 0 }.joinToString("") { names[it] }
                "每周$days ${hhmm(timeOfDay)}"
            }
            ScheduleEngine.RepeatType.INTERVAL ->
                if (windowStart >= 0 && windowEnd > windowStart)
                    "每 $intervalMinutes 分钟 ${hhmm(windowStart)}-${hhmm(windowEnd)}"
                else "每 $intervalMinutes 分钟"
            ScheduleEngine.RepeatType.ONCE ->
                "一次性 " + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(atMillis))
        }
    }
}
