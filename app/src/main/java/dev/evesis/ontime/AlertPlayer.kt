package dev.evesis.ontime

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object AlertPlayer {

    private const val TAG = "OnTime"
    private var mp: MediaPlayer? = null
    private var tts: TextToSpeech? = null

    private fun alarmAttrs() = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    fun play(ctx: Context, r: Reminder, textOverride: String? = null) {
        stop()
        val appCtx = ctx.applicationContext
        val sfx = Sounds.byId(r.soundId)
        val f = Sounds.fileOf(appCtx, sfx)
        if (f == null) {
            playVoice(appCtx, r, textOverride)
            return
        }
        try {
            val p = MediaPlayer()
            p.setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            p.setDataSource(f.absolutePath)
            p.setOnCompletionListener {
                it.release()
                if (mp === it) mp = null
                playVoice(appCtx, r, textOverride)
            }
            p.setOnErrorListener { m, _, _ ->
                try { m.release() } catch (e: Exception) { }
                if (mp === m) mp = null
                playVoice(appCtx, r, textOverride)
                true
            }
            p.prepare()
            mp = p
            p.start()
        } catch (e: Exception) {
            Log.w(TAG, "提示音播放失败,直接进语音", e)
            playVoice(appCtx, r, textOverride)
        }
    }

    private fun playVoice(ctx: Context, r: Reminder, textOverride: String? = null) {
        if (!r.audioUri.isNullOrEmpty()) {
            try {
                val p = MediaPlayer()
                p.setAudioAttributes(alarmAttrs())
                p.setDataSource(ctx, android.net.Uri.parse(r.audioUri))
                p.setOnCompletionListener { it.release() }
                p.prepare()
                mp = p
                p.start()
                return
            } catch (e: Exception) {
                Log.w(TAG, "自定义音乐播放失败,回退语音包", e)
            }
        }

        val speakText = textOverride ?: r.message
        if (r.voiceId.isNotEmpty()) {
            val f = VoicePacks.lookup(ctx, r.voiceId, speakText)
            if (f != null) {
                try {
                    val p = MediaPlayer()
                    p.setAudioAttributes(alarmAttrs())
                    p.setDataSource(f.absolutePath)
                    p.setOnCompletionListener { it.release() }
                    p.prepare()
                    mp = p
                    p.start()
                    return
                } catch (e: Exception) {
                    Log.w(TAG, "语音包播放失败,回退 TTS", e)
                }
            }
        }

        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val t = tts ?: return@TextToSpeech
                try {
                    t.setAudioAttributes(alarmAttrs())
                    t.language = Locale.SIMPLIFIED_CHINESE
                    t.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "ontime-${r.id}")
                } catch (e: Exception) {
                    Log.w(TAG, "TTS 播报失败", e)
                }
            } else {
                Log.w(TAG, "TTS 引擎初始化失败 status=$status")
            }
        }
    }

    fun stop() {
        try { mp?.stop() } catch (e: Exception) { }
        try { mp?.release() } catch (e: Exception) { }
        mp = null
        try { tts?.stop() } catch (e: Exception) { }
        try { tts?.shutdown() } catch (e: Exception) { }
        tts = null
    }
}
