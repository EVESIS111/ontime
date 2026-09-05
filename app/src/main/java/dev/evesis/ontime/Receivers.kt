package dev.evesis.ontime

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("id", -1L)
        if (id <= 0) return
        val db = Db.get(context)
        val r = db.find(id) ?: return

        val now = System.currentTimeMillis()

        if (Alarms.inQuietHours(now)) {
            val end = Alarms.quietEndMillis(now)
            val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, end,
                Alarms.fireIntent(context, id))
            db.logFire(r.id, "${r.title}(免打扰)", r.nextFireAt, now, 0, "none",
                "quiet-deferred→" + java.text.SimpleDateFormat("HH:mm",
                    java.util.Locale.CHINA).format(java.util.Date(end)))
            db.setLastFired(r.id, now)
            db.setNextFire(r.id, end)
            KeepAliveService.refreshNotification(context)
            return
        }

        val chosenMsg = r.pickMessage()
        val planned = if (r.nextFireAt > 0) r.nextFireAt else now
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screen = if (pm.isInteractive) "screen=on" else "screen=off"
        val logId = db.logFire(r.id, r.title, planned, now, now - planned,
            audioLabel(context, r, chosenMsg), "fired | $screen")

        val full = PendingIntent.getActivity(
            context, (2_000_000 + r.id).toInt(),
            Intent(context, AlertActivity::class.java)
                .putExtra("id", r.id).putExtra("log", logId).putExtra("msg", chosenMsg),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val n = android.app.Notification.Builder(context, Channels.REMIND)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(r.title)
            .setContentText(chosenMsg)
            .setCategory(android.app.Notification.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(r.id.toInt(), n)

        try {
            context.startActivity(Intent(context, AlertActivity::class.java)
                .putExtra("id", r.id).putExtra("log", logId).putExtra("msg", chosenMsg)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { /* 全屏意图通知兜底 */ }

        db.setLastFired(r.id, now)
        if (r.repeatType == ScheduleEngine.RepeatType.ONCE) db.setEnabled(r.id, false)
        db.find(id)?.let { Alarms.scheduleNext(context, it) }   // 立刻自续
    }

    private fun audioLabel(context: Context, r: Reminder, msg: String): String =
        Sounds.byId(r.soundId).id + "+" + when {
            !r.audioUri.isNullOrEmpty() -> "music"
            r.voiceId.isEmpty() -> "tts"
            VoicePacks.lookup(context, r.voiceId, msg) != null -> "pack:${r.voiceId}"
            else -> "miss:${r.voiceId}"
        }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Alarms.scheduleAll(context)
                KeepAliveService.start(context)
            }
        }
    }
}

