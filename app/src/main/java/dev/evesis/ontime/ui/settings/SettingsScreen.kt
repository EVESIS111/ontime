package dev.evesis.ontime.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.evesis.ontime.ui.components.WorkspacePage
import dev.evesis.ontime.ui.components.WorkspaceColumns
import dev.evesis.ontime.ui.components.WorkspacePanel
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.evesis.ontime.VoicePacks
import dev.evesis.ontime.data.AlarmHealth
import dev.evesis.ontime.data.AlarmHealthProbe
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeBody
import dev.evesis.ontime.ui.theme.OnTimeCaption
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSecondary
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/** 设置：运行与导入 / 关于与组件目录，沿用统一响应式分区。 */

@Composable
fun SettingsScreen(onBack: () -> Unit, onCatalog: () -> Unit = {}) {
    val context = LocalContext.current
    var imported by remember { mutableStateOf(-1) }
    var importing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?" }
        catch (e: Exception) { "?" }
    }
    val gitSha = remember { dev.evesis.ontime.BuildConfig.GIT_SHA }

    WorkspacePage("设置", "声音、运行状态与应用信息", actions = { QuietButton(onClick = onBack, text = "返回") }) {
        WorkspaceColumns(Modifier.weight(1f), leading = {
            WorkspacePanel("提醒运行状态", "确保每条提醒按时出现") { HealthRow(context) }
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            WorkspacePanel("本地语音包", "导入已有角色声音") {
            Text(
                "把语音文件放入 下载/ontime-voices/(命名:音色id__40位哈希.mp3),然后导入。",
                style = OnTimeBody,
                color = OnTimeColors.InkMuted,
            )
            PixelButton(
                onClick = {
                    importing = true
                    scope.launch {
                        try { imported = withContext(Dispatchers.IO) { VoicePacks.importFromDownload(context.applicationContext) } }
                        finally { importing = false }
                    }
                },
                enabled = !importing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OnTimeSpacing.md),
            ) {
                Text(
                    if (importing) "正在导入…" else "导入语音包",
                    style = OnTimeButtonLabel,
                )
            }

            if (imported >= 0) {
                Text(if (imported > 0) "已导入 $imported 个语音文件" else "未找到可导入的语音包，请检查文件位置和命名。",
                    style = OnTimeSecondary, color = OnTimeColors.Gold)
            }

            }
        }, trailing = {
            WorkspacePanel("关于准时", "只在这台设备上，安心记录日常") {
            Text(
                "准时 v$versionName · 本地提醒,无网络,无账号",
                style = OnTimeBody,
                color = OnTimeColors.InkWhite,
            )
            Text(
                "v$versionName · $gitSha",
                style = OnTimeMetadata,
                color = OnTimeColors.InkMuted,
                modifier = Modifier.padding(top = OnTimeSpacing.md),
            )
            Text(
                "开源:像素字体 Fusion Pixel(OFL)· 滑动确认基于 Brutus(MIT)· 视觉语言参考 Kenney(CC0)",
                style = OnTimeCaption,
                color = OnTimeColors.InkMuted,
                modifier = Modifier.padding(top = OnTimeSpacing.sm),
            )

            }
            Spacer(Modifier.padding(top = OnTimeSpacing.xl))
            WorkspacePanel("组件目录", "查看应用使用的基础控件") {
                QuietButton(onClick = onCatalog, text = "打开组件目录 →")
            }
        })
    }
}

@Composable
private fun HealthRow(context: Context) {
    var health by remember { mutableStateOf(AlarmHealthProbe.probe(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { health = AlarmHealthProbe.probe(context) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                when (health) {
                    AlarmHealth.HEALTHY -> "提醒运行正常"
                    AlarmHealth.DEGRADED -> "需要允许「闹钟和提醒」权限"
                    AlarmHealth.BROKEN -> "通知权限被关闭"
                },
                style = OnTimeBody,
                color = if (health == AlarmHealth.HEALTHY) OnTimeColors.InkWhite else OnTimeColors.Gold,
            )
            if (health != AlarmHealth.HEALTHY) {
                Text(
                    if (health == AlarmHealth.DEGRADED) "否则提醒可能延迟数分钟" else "到点将看不到提醒",
                    style = OnTimeSecondary,
                    color = OnTimeColors.InkMuted,
                    modifier = Modifier.padding(top = OnTimeSpacing.xxs),
                )
            }
        }
        if (health != AlarmHealth.HEALTHY) {
            QuietButton(
                onClick = { AlarmHealthProbe.openSettings(context) },
                text = "去开启",
                emphasize = true,
            )
        }
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Composable
private fun SettingsPreview() {
    OnTimeTheme { SettingsScreen(onBack = {}) }
}
