package dev.evesis.ontime

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Channels {
    const val REMIND = "ontime_remind"   // 高优先级,无声(声音由播报链负责)
    const val STATUS = "ontime_status"   // 常驻状态,最低打扰

    fun ensure(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            android.app.NotificationChannel(REMIND, "到点提醒",
                NotificationManager.IMPORTANCE_HIGH).apply { setSound(null, null) })
        nm.createNotificationChannel(
            android.app.NotificationChannel(STATUS, "常驻运行状态",
                NotificationManager.IMPORTANCE_MIN))
    }
}

object Alarms {

    const val QUIET_START_MIN = ScheduleEngine.QUIET_START_MIN   // 00:00
    const val QUIET_END_MIN = ScheduleEngine.QUIET_END_MIN       // 08:30

    fun inQuietHours(now: Long = System.currentTimeMillis()): Boolean =
        ScheduleEngine.inQuietHours(now)

    fun quietEndMillis(now: Long = System.currentTimeMillis()): Long =
        ScheduleEngine.quietEnd(now)

    fun fireIntent(ctx: Context, id: Long): PendingIntent =
        PendingIntent.getBroadcast(
            ctx, (1_000_000 + id).toInt(),
            Intent(ctx, AlarmReceiver::class.java).putExtra("id", id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun scheduleNext(ctx: Context, r: Reminder) {
        val db = Db.get(ctx)
        if (!r.enabled) {
            cancel(ctx, r.id); db.setNextFire(r.id, 0); return
        }
        val now = System.currentTimeMillis()
        val next = ScheduleEngine.next(r.rule(), now)
        if (next == null) {                      // ONCE 过期 → 自动停用,防重复
            cancel(ctx, r.id); db.setNextFire(r.id, 0)
            if (r.repeatType == ScheduleEngine.RepeatType.ONCE) db.setEnabled(r.id, false)
            return
        }
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val show = PendingIntent.getActivity(
            ctx, r.id.toInt(), Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // 实测(2026-09-05,HarmonyOS 4.2/Android 12 基座):SCHEDULE_EXACT_ALARM 为 AppOps 型,
        // 覆盖安装/重置后可能被系统静默撤销,setAlarmClock 直接抛 SecurityException 导致进程崩溃。
        // 降级链:setAlarmClock(exact) → setExactAndAllowWhileIdle → set(非精确,保可用)。
        try {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(next, show), fireIntent(ctx, r.id))
        } catch (se: SecurityException) {
            // 可观测性:降级不等于健康,fire_log 留痕(未来排查"为什么迟到"可定位)
            Db.get(ctx).logFire(r.id, "${r.title}(调度降级)", next, System.currentTimeMillis(), 0,
                "mode", "fallback-inexact cap=exact-denied")
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, fireIntent(ctx, r.id))
            } catch (se2: SecurityException) {
                am.setWindow(AlarmManager.RTC_WAKEUP, next, 60_000L, fireIntent(ctx, r.id))
            }
        }
        db.setNextFire(r.id, next)
        KeepAliveService.refreshNotification(ctx)
    }

    fun scheduleAll(ctx: Context) {
        val list = Db.get(ctx).list()
        list.forEach { scheduleNext(ctx, it) }
    }

    fun snooze(ctx: Context, id: Long, minutes: Int) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val at = System.currentTimeMillis() + minutes * 60_000L
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, fireIntent(ctx, id))
        } catch (se: SecurityException) {   // exact 权限被撤销时降级,保"稍后"可用
            am.setWindow(AlarmManager.RTC_WAKEUP, at, 60_000L, fireIntent(ctx, id))
        }
    }

    fun cancel(ctx: Context, id: Long) {
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(fireIntent(ctx, id))
    }
}

class KeepAliveService : android.app.Service() {

    override fun onBind(intent: Intent?): android.os.IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Channels.ensure(this)
        startForeground(SID, buildNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Alarms.scheduleAll(this)   // 服务被拉起时顺手自愈一次闹钟
        return START_STICKY
    }

    companion object {
        const val SID = 42

        fun start(ctx: Context) {
            val i = Intent(ctx, KeepAliveService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i)
            else ctx.startService(i)
        }

        fun refreshNotification(ctx: Context) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try { nm.notify(SID, buildNotification(ctx)) } catch (e: Exception) { /* 服务未起时忽略 */ }
        }

        fun buildNotification(ctx: Context): Notification {
            val soonest = Db.get(ctx).list()
                .filter { it.enabled && it.nextFireAt > 0 }
                .minByOrNull { it.nextFireAt }
            val text = if (soonest == null) {
                "运行中 · 暂无计划提醒"
            } else {
                val remain = soonest.nextFireAt - System.currentTimeMillis()
                val fmt = SimpleDateFormat("HH:mm", Locale.CHINA)
                "运行中 · 下次「${soonest.title}」${fmt.format(Date(soonest.nextFireAt))}(还有${humanize(remain)})"
            }
            val pi = PendingIntent.getActivity(
                ctx, 3, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            return Notification.Builder(ctx, Channels.STATUS)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("准时 · 常驻运行")
                .setContentText(text)
                .setOngoing(true)
                .setContentIntent(pi)
                .build()
        }

        fun humanize(ms: Long): String {
            val m = (ms / 60_000L).coerceAtLeast(0)
            return if (m >= 60) "${m / 60}小时${m % 60}分" else "${m}分钟"
        }
    }
}
