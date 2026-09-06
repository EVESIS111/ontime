package dev.evesis.ontime

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.evesis.ontime.ui.alert.AlertScreen
import dev.evesis.ontime.ui.theme.OnTimeTheme

/**
 * 到点提醒页(Compose 版)。
 * 行为契约与 v10.3 View 版一致:extras id/log;showWhenLocked/turnScreenOn/KEEP_SCREEN_ON;
 * 音量键 STREAM_ALARM;滑动 ≥90% slide-ack;稍后 snoozed;120s timeout;onNewIntent 重载;
 * onDestroy 停播。旧 5-id(res/values/ids.xml)随 View 壳退役,新契约=AlertScreen 参数。
 */
class AlertActivity : ComponentActivity() {

    private var reminderId = -1L
    private var logId = -1L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setVolumeControlStream(AudioManager.STREAM_ALARM)
        load(intent.getLongExtra("id", -1), intent.getLongExtra("log", -1))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        AlertPlayer.stop()
        load(intent.getLongExtra("id", -1), intent.getLongExtra("log", -1))
    }

    private fun load(id: Long, log: Long) {
        reminderId = id; logId = log
        val r = Db.get(this).find(id) ?: run { finish(); return }

        setContent {
            OnTimeTheme {
                AlertScreen(
                    title = r.title,
                    message = r.pickMessage(),
                    voiceLabel = if (r.voiceId.isNotEmpty()) "${VoicePacks.displayName(r.voiceId)} 说" else "准时",
                    snoozeLabel = "稍后 ${r.snoozeMinutes} 分钟",
                    onSlideAck = {
                        Db.get(this).updateLogNote(logId, "slide-ack")
                        finish()
                    },
                    onSnooze = {
                        Alarms.snooze(this, r.id, r.snoozeMinutes)
                        Db.get(this).updateLogNote(logId, "snoozed")
                        finish()
                    },
                )
            }
        }

        Db.get(this).updateLogNote(logId, "ui=shown")
        AlertPlayer.play(this, r)
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            Db.get(this).updateLogNote(logId, "timeout")
            finish()
        }, 120_000L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        AlertPlayer.stop()
        super.onDestroy()
    }
}
