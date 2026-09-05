package dev.evesis.ontime

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class AlertActivity : Activity() {

    private var reminderId = -1L
    private var logId = -1L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var slideTrack: View
    private lateinit var btnSlide: View

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

        val title = TextView(this).apply { this.id = R.id.alertTitle; text = r.title; textSize = 32f }
        val message = TextView(this).apply { this.id = R.id.alertMessage; text = r.pickMessage(); textSize = 20f }
        val snooze = TextView(this).apply {
            this.id = R.id.btnSnooze; text = "稍后 ${r.snoozeMinutes} 分钟"; textSize = 16f
            setOnClickListener {
                Alarms.snooze(this@AlertActivity, r.id, r.snoozeMinutes)
                Db.get(this@AlertActivity).updateLogNote(logId, "snoozed")
                finish()
            }
        }
        slideTrack = FrameLayout(this).apply {
            this.id = R.id.slideTrack; setBackgroundColor(0xFF1F3560.toInt())
            addView(TextView(this@AlertActivity).apply {
                text = "滑动确认"; textSize = 18f; setTextColor(0xFF6E86B8.toInt())
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        }
        btnSlide = FrameLayout(this).apply {
            this.id = R.id.btnSlide; setBackgroundColor(0xFF3E6AB0.toInt())
            contentDescription = "按住滑块划到最右确认"
            addView(TextView(this@AlertActivity).apply {
                text = "→"; textSize = 24f; gravity = Gravity.CENTER
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        }
        (slideTrack as FrameLayout).addView(btnSlide, FrameLayout.LayoutParams(160, 128,
            Gravity.START or Gravity.CENTER_VERTICAL).apply { marginStart = 16 })

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(title, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER_HORIZONTAL })
            addView(message, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = 48 })
            (slideTrack as View).layoutParams = LinearLayout.LayoutParams(900, 160).apply {
                gravity = Gravity.CENTER_HORIZONTAL; topMargin = 72 }
            addView(slideTrack)
            addView(snooze, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = 40 })
        })

        Db.get(this).updateLogNote(logId, "ui=shown")
        AlertPlayer.play(this, r)
        handler.postDelayed({ Db.get(this).updateLogNote(logId, "timeout"); finish() }, 120_000L)

        var startX = 0f; var maxSlide = 0f
        btnSlide.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    maxSlide = (slideTrack.width - v.width - 12f).coerceAtLeast(0f)
                    startX = e.rawX - v.translationX; true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.translationX = (e.rawX - startX).coerceIn(0f, maxSlide); true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (maxSlide > 0 && v.translationX >= maxSlide * 0.9f) {
                        Db.get(this@AlertActivity).updateLogNote(logId, "slide-ack")
                        finish()
                    } else v.animate().translationX(0f).setDuration(180).start()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        AlertPlayer.stop()
        super.onDestroy()
    }
}
