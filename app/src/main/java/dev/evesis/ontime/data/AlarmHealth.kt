package dev.evesis.ontime.data

import android.app.AlarmManager
import android.content.Context
import android.os.Build

/**
 * 提醒运行健康度(产品状态,与平台能力解耦;未来进 shared 时保留枚举,Android 层只留 provider)。
 * HEALTHY  = exact alarm 可用,提醒按产品承诺准时调度
 * DEGRADED = App 仍能提醒,但 exact 权限被撤销,只能走不精确 fallback(可能延迟数分钟)
 * BROKEN   = 连提醒通道都不完整(如通知权限缺失),到点用户看不到
 */
enum class AlarmHealth { HEALTHY, DEGRADED, BROKEN }

/** Android 平台能力封装:唯一允许触碰 AlarmManager/NotificationManager 的健康检查点 */
object AlarmHealthProbe {

    fun probe(ctx: Context): AlarmHealth {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) return AlarmHealth.DEGRADED

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (Build.VERSION.SDK_INT >= 33 &&
            !nm.areNotificationsEnabled()) return AlarmHealth.BROKEN

        return AlarmHealth.HEALTHY
    }

    /** 「去开启」的目标:exact alarm 特殊授权页(Android 12+);无权限页则回应用详情 */
    fun openSettings(ctx: Context) {
        try {
            ctx.startActivity(android.content.Intent(
                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                android.net.Uri.parse("package:dev.evesis.ontime"),
            ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            // 个别 ROM 无此授权页 → 回退应用详情页
            try {
                ctx.startActivity(android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.parse("package:dev.evesis.ontime"),
                ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e2: Exception) { /* 无可跳转,UI 提示已存在 */ }
        }
    }
}
