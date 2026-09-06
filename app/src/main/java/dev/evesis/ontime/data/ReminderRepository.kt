package dev.evesis.ontime.data

import android.content.Context
import dev.evesis.ontime.Alarms
import dev.evesis.ontime.Db
import dev.evesis.ontime.Reminder

/**
 * 数据访问统一边界:所有 UI 层经此操作提醒,避免各页面直连 Db/Alarms。
 * 写操作后自动重调度(scheduleNext 覆盖 enabled/disabled/ONCE 过期全状态)。
 */
class ReminderRepository private constructor(ctx: Context) {

    private val appCtx = ctx.applicationContext

    fun list(): List<Reminder> = Db.get(appCtx).list()
    fun find(id: Long): Reminder? = Db.get(appCtx).find(id)

    fun insert(r: Reminder): Long =
        Db.get(appCtx).insert(r).also { id ->
            Db.get(appCtx).find(id)?.let { Alarms.scheduleNext(appCtx, it) }
        }

    fun update(r: Reminder) {
        Db.get(appCtx).update(r)
        Alarms.scheduleNext(appCtx, r)
    }

    fun delete(r: Reminder) {
        Alarms.cancel(appCtx, r.id)
        Db.get(appCtx).delete(r.id)
    }

    fun setEnabled(id: Long, on: Boolean) {
        Db.get(appCtx).setEnabled(id, on)
        Db.get(appCtx).find(id)?.let { Alarms.scheduleNext(appCtx, it) }
    }

    fun alarmHealth(): AlarmHealth = AlarmHealthProbe.probe(appCtx)

    companion object {
        @Volatile private var inst: ReminderRepository? = null
        fun get(ctx: Context): ReminderRepository =
            inst ?: synchronized(this) {
                inst ?: ReminderRepository(ctx).also { inst = it }
            }
    }
}
