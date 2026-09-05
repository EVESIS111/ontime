package dev.evesis.ontime

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Reminder(
    val id: Long,
    val title: String,
    val message: String,
    val audioUri: String?,      // 自定义音乐 content uri,优先级最高
    val voiceId: String,        // "" = 跟随系统 TTS
    val soundId: String,        // 游戏提示音(Sounds.ALL)
    val repeatType: ScheduleEngine.RepeatType,
    val timeOfDay: Int,         // 当日分钟数 0..1439(INTERVAL 时作为循环开始)
    val weekMask: Int,          // bit(dow-1),周日=1
    val intervalMinutes: Int,
    val atMillis: Long,
    val snoozeMinutes: Int,
    val enabled: Boolean,
    val nextFireAt: Long,
    val lastFiredAt: Long,
    val windowStart: Int = -1,  // INTERVAL 日窗口开始分钟数,-1 = 无窗口
    val windowEnd: Int = -1,    // INTERVAL 日窗口结束分钟数
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

class Db private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx.applicationContext, "ontime.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE reminders(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL, message TEXT NOT NULL,
                audio_uri TEXT, voice_id TEXT NOT NULL DEFAULT '',
                sound_id TEXT NOT NULL DEFAULT 'coin',
                repeat_type TEXT NOT NULL,
                time_of_day INTEGER NOT NULL DEFAULT 0,
                week_mask INTEGER NOT NULL DEFAULT 0,
                interval_minutes INTEGER NOT NULL DEFAULT 60,
                at_millis INTEGER NOT NULL DEFAULT 0,
                snooze_minutes INTEGER NOT NULL DEFAULT 10,
                enabled INTEGER NOT NULL DEFAULT 1,
                next_fire_at INTEGER NOT NULL DEFAULT 0,
                last_fired_at INTEGER NOT NULL DEFAULT 0,
                window_start INTEGER NOT NULL DEFAULT -1,
                window_end INTEGER NOT NULL DEFAULT -1)""")
        db.execSQL(
            """CREATE TABLE fire_log(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                reminder_id INTEGER NOT NULL, title TEXT NOT NULL,
                planned_at INTEGER NOT NULL, actual_at INTEGER NOT NULL,
                latency_ms INTEGER NOT NULL, audio TEXT NOT NULL, note TEXT NOT NULL)""")
        db.execSQL("CREATE INDEX idx_fire_actual ON fire_log(actual_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE reminders ADD COLUMN sound_id TEXT NOT NULL DEFAULT 'coin'")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE reminders ADD COLUMN window_start INTEGER NOT NULL DEFAULT -1")
            db.execSQL("ALTER TABLE reminders ADD COLUMN window_end INTEGER NOT NULL DEFAULT -1")
        }
    }


    private fun rowToReminder(c: android.database.Cursor): Reminder = Reminder(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        title = c.getString(c.getColumnIndexOrThrow("title")),
        message = c.getString(c.getColumnIndexOrThrow("message")),
        audioUri = c.getString(c.getColumnIndexOrThrow("audio_uri")),
        voiceId = c.getString(c.getColumnIndexOrThrow("voice_id")),
        soundId = c.getString(c.getColumnIndexOrThrow("sound_id")),
        repeatType = ScheduleEngine.RepeatType.valueOf(c.getString(c.getColumnIndexOrThrow("repeat_type"))),
        timeOfDay = c.getInt(c.getColumnIndexOrThrow("time_of_day")),
        weekMask = c.getInt(c.getColumnIndexOrThrow("week_mask")),
        intervalMinutes = c.getInt(c.getColumnIndexOrThrow("interval_minutes")),
        atMillis = c.getLong(c.getColumnIndexOrThrow("at_millis")),
        snoozeMinutes = c.getInt(c.getColumnIndexOrThrow("snooze_minutes")),
        enabled = c.getInt(c.getColumnIndexOrThrow("enabled")) != 0,
        nextFireAt = c.getLong(c.getColumnIndexOrThrow("next_fire_at")),
        lastFiredAt = c.getLong(c.getColumnIndexOrThrow("last_fired_at")),
        windowStart = c.getInt(c.getColumnIndexOrThrow("window_start")),
        windowEnd = c.getInt(c.getColumnIndexOrThrow("window_end")),
    )

    fun list(): MutableList<Reminder> {
        val out = mutableListOf<Reminder>()
        readableDatabase.query("reminders", null, null, null, null, null, "id ASC").use {
            while (it.moveToNext()) out.add(rowToReminder(it))
        }
        return out
    }

    fun find(id: Long): Reminder? =
        readableDatabase.query("reminders", null, "id=?", arrayOf(id.toString()),
            null, null, null).use { if (it.moveToFirst()) rowToReminder(it) else null }

    fun insert(r: Reminder): Long = writableDatabase.insert("reminders", null, r.toValues())

    fun update(r: Reminder) =
        writableDatabase.update("reminders", r.toValues(), "id=?", arrayOf(r.id.toString()))

    fun delete(id: Long) = writableDatabase.delete("reminders", "id=?", arrayOf(id.toString()))

    fun setNextFire(id: Long, t: Long) = exec("UPDATE reminders SET next_fire_at=$t WHERE id=$id")
    fun setLastFired(id: Long, t: Long) = exec("UPDATE reminders SET last_fired_at=$t WHERE id=$id")
    fun setEnabled(id: Long, on: Boolean) = exec("UPDATE reminders SET enabled=${if (on) 1 else 0} WHERE id=$id")

    private fun exec(sql: String) = writableDatabase.execSQL(sql)

    private fun Reminder.toValues(): ContentValues = ContentValues().apply {
        put("title", title); put("message", message)
        put("audio_uri", audioUri); put("voice_id", voiceId); put("sound_id", soundId)
        put("repeat_type", repeatType.name)
        put("time_of_day", timeOfDay); put("week_mask", weekMask)
        put("interval_minutes", intervalMinutes); put("at_millis", atMillis)
        put("snooze_minutes", snoozeMinutes); put("enabled", if (enabled) 1 else 0)
        put("next_fire_at", nextFireAt); put("last_fired_at", lastFiredAt)
        put("window_start", windowStart); put("window_end", windowEnd)
    }

    fun seedDefaultsIfEmpty() {
        if (list().isNotEmpty()) return
        insert(Reminder(0, "喝水", "该喝水了,起来活动一下,补充水分。", null,
            "daji", "potion", ScheduleEngine.RepeatType.INTERVAL,
            0, 0, 45, 0, 5, true, 0, 0, 540, 1290))
        insert(Reminder(0, "学英语", "英语学习时间到了,坚持每天进步一点点。", null,
            "zhaojun", "powerup", ScheduleEngine.RepeatType.DAILY,
            13 * 60, 0, 60, 0, 5, true, 0, 0))
        insert(Reminder(0, "下班", "下班时间到,收拾好东西,好好休息。", null,
            "xiaoqiao", "jingle", ScheduleEngine.RepeatType.WEEKLY,
            21 * 60 + 30, 0b0111110 /*周一~周五*/, 60, 0, 5, true, 0, 0))
        insert(Reminder(0, "科目四", "科目四学习时间到了,先把题库刷起来,错题过一遍,稳住心态,一把就过。", null,
            "zh-CN-YunyangNeural", "key", ScheduleEngine.RepeatType.DAILY,
            11 * 60 + 30, 0, 60, 0, 5, true, 0, 0))
    }


    fun logFire(reminderId: Long, title: String, planned: Long, actual: Long,
                latency: Long, audio: String, note: String): Long {
        val id = writableDatabase.insert("fire_log", null, ContentValues().apply {
            put("reminder_id", reminderId); put("title", title)
            put("planned_at", planned); put("actual_at", actual)
            put("latency_ms", latency); put("audio", audio); put("note", note)
        })
        writableDatabase.execSQL(
            "DELETE FROM fire_log WHERE id NOT IN (SELECT id FROM fire_log ORDER BY actual_at DESC LIMIT 1000)")
        return id
    }

    fun updateLogNote(logId: Long, note: String) {
        if (logId <= 0) return
        writableDatabase.execSQL("UPDATE fire_log SET note=note||' | '||'$note' WHERE id=$logId")
    }

    companion object {
        @Volatile private var inst: Db? = null
        fun get(ctx: Context): Db =
            inst ?: synchronized(this) { inst ?: Db(ctx).also { inst = it } }
    }
}
