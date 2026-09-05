package dev.evesis.ontime

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Channels.ensure(this)
        VoicePacks.installBundled(this)
        Sounds.ensureInstalled(this)
        Db.get(this).seedDefaultsIfEmpty()
        Alarms.scheduleAll(this)
        KeepAliveService.start(this)

        val list = Db.get(this).list()
        val soonest = list.filter { it.enabled && it.nextFireAt > 0 }.minByOrNull { it.nextFireAt }
        setContentView(TextView(this).apply {
            textSize = 16f
            setPadding(48, 96, 48, 48)
            text = "准时 · 后端核心运行中\n\n" +
                "已注册提醒:${list.count { it.enabled }} 个\n" +
                (soonest?.let { "下次:${it.title} " + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(it.nextFireAt)) }
                    ?: "暂无计划中的提醒")
        })
    }
}
