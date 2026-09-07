package dev.evesis.ontime.ui.editor

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper
import dev.evesis.ontime.Sounds
import dev.evesis.ontime.VoicePacks
import java.io.File
import java.util.Locale

/** Editor-only playback; never route a voice preview through the sound effect. */
object PreviewPlayer {
    private var mp: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var source: String? = null
    private var generation = 0

    private fun begin(key: String, onMessage: (String?) -> Unit): Boolean {
        val same = source == key
        stop()
        onMessage(null)
        if (same) return false
        source = key
        return true
    }

    fun toggleSound(ctx: Context, soundId: String, onMessage: (String?) -> Unit) {
        if (!begin("sound:$soundId", onMessage)) return
        val file = Sounds.fileOf(ctx.applicationContext, Sounds.byId(soundId))
        if (file == null) { stop(); onMessage("此音效为静音"); return }
        playFile(file, onMessage)
    }

    fun toggleVoice(ctx: Context, voiceId: String, text: String, onMessage: (String?) -> Unit) {
        if (!begin("voice:$voiceId:$text", onMessage)) return
        if (text.isBlank()) { stop(); onMessage("请先填写台词或标题"); return }
        val file = VoicePacks.lookup(ctx.applicationContext, voiceId, text)
        if (file != null) { playFile(file, onMessage); return }
        onMessage("该台词使用系统语音")
        val token = generation
        tts = TextToSpeech(ctx.applicationContext) { status ->
            if (token == generation) {
                val player = tts
                if (status != TextToSpeech.SUCCESS || player == null) {
                    stop(); onMessage("系统语音暂不可用")
                } else {
                    player.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) { complete(false) }
                        @Deprecated("Platform callback")
                        override fun onError(utteranceId: String?) { complete(true) }
                        private fun complete(failed: Boolean) {
                            Handler(Looper.getMainLooper()).post {
                                if (token == generation) {
                                    stop()
                                    if (failed) onMessage("系统语音暂不可用")
                                }
                            }
                        }
                    })
                    try {
                        val language = player.setLanguage(Locale.SIMPLIFIED_CHINESE)
                        if (language < 0 || player.speak(text, TextToSpeech.QUEUE_FLUSH, null, "preview") == TextToSpeech.ERROR) {
                            stop(); onMessage("系统语音暂不可用")
                        }
                    } catch (e: Exception) { stop(); onMessage("系统语音暂不可用") }
                }
            }
        }
    }

    private fun playFile(file: File, onMessage: (String?) -> Unit) {
        val player = MediaPlayer()
        mp = player
        try {
            player.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            player.setDataSource(file.absolutePath)
            player.setOnPreparedListener { if (mp === it) it.start() }
            player.setOnCompletionListener { if (mp === it) stop() }
            player.setOnErrorListener { failed, _, _ ->
                if (mp === failed) { stop(); onMessage("试听播放失败") }
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            stop(); onMessage("试听播放失败")
        }
    }

    fun stop() {
        generation++
        try { mp?.release() } catch (e: Exception) { }
        mp = null
        try { tts?.stop(); tts?.shutdown() } catch (e: Exception) { }
        tts = null
        source = null
    }
}
