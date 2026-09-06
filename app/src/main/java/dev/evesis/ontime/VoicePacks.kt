package dev.evesis.ontime

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object VoicePacks {

    /** 角色显示名(Alert 角色席位/编辑页音色显示;纯函数,shared-ready) */
    fun displayName(voiceId: String): String = when (voiceId) {
        "daji" -> "妲己"
        "zhaojun" -> "王昭君"
        "xiaoqiao" -> "小乔"
        "diaochan" -> "貂蝉"
        "zh-CN-YunyangNeural" -> "云扬"
        "zh-CN-XiaoxiaoNeural" -> "晓晓"
        "zh-CN-YunjianNeural" -> "云健"
        "" -> "准时"
        else -> voiceId
    }

    private const val TAG = "OnTime"

    fun dir(ctx: Context): File = File(ctx.filesDir, "voices")

    fun sha1(s: String): String =
        MessageDigest.getInstance("SHA-1").digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun lookup(ctx: Context, voiceId: String, text: String): File? {
        if (voiceId.isEmpty()) return null
        val name = sha1(text)
        for (ext in listOf("mp3", "wav", "m4a")) {   // 语音包管线多来源:edge-tts=mp3,本地推理=wav/m4a
            val f = File(File(dir(ctx), voiceId), "$name.$ext")
            if (f.isFile) return f
        }
        return null
    }


    fun installBundled(ctx: Context): Int {
        var n = 0
        try {
            val voices = ctx.assets.list("voices") ?: return 0
            for (v in voices) {
                val target = File(dir(ctx), v).apply { mkdirs() }
                for (name in ctx.assets.list("voices/$v") ?: continue) {
                    val out = File(target, name)
                    if (out.isFile) continue
                    ctx.assets.open("voices/$v/$name").use { input ->
                        FileOutputStream(out).use { input.copyTo(it) }
                    }
                    n++
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "内置语音包安装失败", e)
        }
        return n
    }

    fun importFromDownload(ctx: Context): Int {
        val src = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ontime-voices")
        var n = 0
        for (f in src.listFiles() ?: return 0) {
            val name = f.name
            if (!name.endsWith(".mp3") && !name.endsWith(".wav") && !name.endsWith(".m4a")) continue
            val stem = name.substringBeforeLast('.')
            val ext = name.substringAfterLast('.')
            val i = stem.indexOf("__")
            if (i <= 0) continue
            val voice = stem.substring(0, i)
            val hash = stem.substring(i + 2)
            if (hash.length != 40) continue
            val target = File(File(dir(ctx), voice), "$hash.$ext").apply { parentFile?.mkdirs() }
            try {
                if (!f.renameTo(target)) {
                    f.inputStream().use { input ->
                        FileOutputStream(target).use { input.copyTo(it) }
                    }
                    f.delete()
                }
                n++
            } catch (e: Exception) {
                Log.w(TAG, "导入语音失败: $name", e)
            }
        }
        return n
    }
}
