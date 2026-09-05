package dev.evesis.ontime

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object Sounds {

    data class Sfx(val id: String, val label: String, val file: String)

    val ALL = listOf(
        Sfx("none", "无提示音", ""),
        Sfx("coin", "金币 ★", "sfx_coin.wav"),
        Sfx("powerup", "能力提升", "sfx_powerup.wav"),
        Sfx("alarm8", "8bit警报", "sfx_alarm8.wav"),
        Sfx("jingle", "胜利旋律", "sfx_jingle.wav"),
        Sfx("1up", "1UP", "sfx_1up.wav"),
        Sfx("fanfare", "战斗号角", "sfx_fanfare.wav"),
        Sfx("potion", "药剂气泡", "sfx_potion.wav"),
        Sfx("key", "关键道具", "sfx_key.wav"),
        Sfx("sword", "剑击", "sfx_sword.wav"),
        Sfx("save", "存档确认", "sfx_save.wav"),
        Sfx("menu", "菜单琶音", "sfx_menu.wav"),
        Sfx("confirm", "确认叮", "sfx_confirm.ogg"),
        Sfx("error", "错误音", "sfx_error.ogg"),
        Sfx("glass", "水晶铃", "glass_001.ogg"),
        Sfx("bong", "钟鸣", "bong_001.ogg"),
        Sfx("herald", "号角短", "confirmation_002.ogg"),
        Sfx("rise", "上升音", "maximize_001.ogg"),
        Sfx("fall", "下降音", "minimize_001.ogg"),
        Sfx("mystery", "神秘音", "question_001.ogg"),
        Sfx("pluck", "拨弦", "select_001.ogg"),
        Sfx("toggle", "切换音", "switch_002.ogg"),
    )

    fun byId(id: String?): Sfx = ALL.firstOrNull { it.id == id } ?: ALL[1]

    fun dir(ctx: Context): File = File(ctx.filesDir, "sounds")

    fun ensureInstalled(ctx: Context): Int {
        val d = dir(ctx).apply { mkdirs() }
        var n = 0
        for (s in ALL) {
            if (s.file.isEmpty()) continue
            val out = File(d, s.file)
            if (out.isFile) continue
            try {
                ctx.assets.open("sounds/${s.file}").use { input ->
                    FileOutputStream(out).use { input.copyTo(it) }
                }
                n++
            } catch (e: Exception) {
                android.util.Log.w("OnTime", "提示音释放失败 ${s.file}", e)
            }
        }
        return n
    }

    fun fileOf(ctx: Context, s: Sfx): File? {
        if (s.file.isEmpty()) return null
        val f = File(dir(ctx), s.file)
        return if (f.isFile) f else null
    }
}
