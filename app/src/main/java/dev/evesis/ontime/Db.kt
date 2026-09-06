package dev.evesis.ontime

import android.content.Context
import androidx.room.Room

/**
 * 数据门面(PHASE8 起:对外 API 与旧 SQLiteOpenHelper 版逐签名一致,内部换 Room)。
 * 所有既有调用点(Alarms/Receivers/AlertActivity/Repository/MainActivity)零改动。
 * 主线程访问暂保留(allowMainThreadQueries):数据量 4-1000 行、查询 <1ms,
 * 全面 suspend 化推迟到 KMP shared 化时一次完成(DECISIONS 2026-09-06)。
 */
class Db private constructor(ctx: Context) {

    private val db: OnTimeDatabase = Room.databaseBuilder(
        ctx.applicationContext, OnTimeDatabase::class.java, "ontime.db")
        .addMigrations(MIGRATION_3_4)
        .allowMainThreadQueries()
        .build()

    private val r get() = db.reminderDao()
    private val f get() = db.fireLogDao()

    fun list(): List<Reminder> = r.list()
    fun find(id: Long): Reminder? = r.find(id)
    fun insert(reminder: Reminder): Long = r.insert(reminder)
    fun update(reminder: Reminder) = r.update(reminder)
    fun delete(id: Long) = r.delete(id)

    fun setNextFire(id: Long, t: Long) = r.setNextFire(id, t)
    fun setLastFired(id: Long, t: Long) = r.setLastFired(id, t)
    fun setEnabled(id: Long, on: Boolean) {
        // 归零事件(09-05/09-06 两次未归因)追踪:记录调用方,写入失败也不阻断主操作
        try {
            f.insert(FireLogEntity(reminderId = id, title = "[AUDIT]",
                plannedAt = 0, actualAt = System.currentTimeMillis(), latencyMs = 0,
                audio = if (on) "on" else "off",
                note = Throwable().stackTrace.drop(1).take(4).joinToString("|") { it.className.substringAfterLast('.') + "." + it.methodName }))
            f.trimTo1000()
        } catch (e: Exception) { }
        r.setEnabled(id, on)
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
        val id = f.insert(FireLogEntity(reminderId = reminderId, title = title,
            plannedAt = planned, actualAt = actual, latencyMs = latency, audio = audio, note = note))
        f.trimTo1000()
        return id
    }

    fun updateLogNote(logId: Long, note: String) {
        if (logId <= 0) return
        f.appendNote(logId, note)
    }

    companion object {
        @Volatile private var inst: Db? = null
        fun get(ctx: Context): Db =
            inst ?: synchronized(this) { inst ?: Db(ctx).also { inst = it } }
    }
}
