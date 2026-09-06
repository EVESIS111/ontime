package dev.evesis.ontime.ui.editor

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dev.evesis.ontime.Sounds
import dev.evesis.ontime.VoicePacks

/**
 * 编辑页试听播放器(v11.3 §18-19:Sound/Voice 操作必须有完整反馈)。
 * 独立于 AlertPlayer(提醒播放管线冻结不动);UI 层轻量实现:音效→语音包二级,
 * ▶/■ 即时切换,失败返回 false(内联提示"不可用",不静默)。
 */
object PreviewPlayer {
    private var mp: MediaPlayer? = null

    fun isPlaying(): Boolean = mp?.isPlaying == true

    /** 返回 true=开始播放;false=资源不可用(调用方显示内联错误) */
    fun toggle(ctx: Context, soundId: String, voiceId: String, text: String): Boolean {
        if (isPlaying()) { stop(); return false }
        val appCtx = ctx.applicationContext
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
        // 一级:音效文件
        val sfx = Sounds.fileOf(appCtx, Sounds.byId(soundId))
        if (sfx != null) {
            try {
                val p = MediaPlayer(); p.setAudioAttributes(attrs); p.setDataSource(sfx.absolutePath)
                p.setOnCompletionListener { stop(); }
                p.prepare(); mp = p; p.start(); return true
            } catch (e: Exception) { stop() }
        }
        // 二级:语音包该台词文件
        if (voiceId.isNotEmpty()) {
            val f = VoicePacks.lookup(appCtx, voiceId, text)
            if (f != null) {
                try {
                    val p = MediaPlayer(); p.setAudioAttributes(attrs); p.setDataSource(f.absolutePath)
                    p.setOnCompletionListener { stop(); }
                    p.prepare(); mp = p; p.start(); return true
                } catch (e: Exception) { stop() }
            }
        }
        return false
    }

    fun stop() {
        try { mp?.stop() } catch (e: Exception) { }
        try { mp?.release() } catch (e: Exception) { }
        mp = null
    }
}
