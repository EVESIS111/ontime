package dev.evesis.ontime.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.VoicePacks
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelPanel
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSpacing

/** 设置页(最小版:语音包导入 + 版本/许可信息;后续按需扩展) */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var imported by remember { mutableStateOf(-1) }
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?" }
        catch (e: Exception) { "?" }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .verticalScroll(rememberScrollState())
            .padding(OnTimeSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = OnTimeSpacing.contentMaxWidth).fillMaxWidth()) {
            Text(
                "设置", style = MaterialTheme.typography.titleLarge, color = OnTimeColors.Gold,
                modifier = Modifier.padding(bottom = OnTimeSpacing.md),
            )

            // 提醒运行状态(Alarm Health;用户语言,非工程调试面板)
            PixelPanel(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.md)) {
                val health = dev.evesis.ontime.data.AlarmHealthProbe.probe(context)
                Text("提醒运行状态", style = MaterialTheme.typography.titleMedium, color = OnTimeColors.Gold)
                Text(
                    when (health) {
                        dev.evesis.ontime.data.AlarmHealth.HEALTHY -> "提醒运行正常"
                        dev.evesis.ontime.data.AlarmHealth.DEGRADED -> "需要允许「闹钟和提醒」权限,否则提醒可能延迟数分钟"
                        dev.evesis.ontime.data.AlarmHealth.BROKEN -> "通知权限被关闭,到点将看不到提醒"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (health == dev.evesis.ontime.data.AlarmHealth.HEALTHY) OnTimeColors.InkWhite else OnTimeColors.Gold,
                )
                if (health != dev.evesis.ontime.data.AlarmHealth.HEALTHY) {
                    PixelButton(
                        onClick = { dev.evesis.ontime.data.AlarmHealthProbe.openSettings(context) },
                        modifier = Modifier.fillMaxWidth().padding(top = OnTimeSpacing.sm),
                    ) { Text("去开启", style = MaterialTheme.typography.bodyLarge) }
                }
            }

            PixelPanel(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.md)) {
                Text("语音包", style = MaterialTheme.typography.titleMedium, color = OnTimeColors.Gold)
                Text(
                    "把语音文件放入 下载/ontime-voices/(命名:音色id__40位哈希.mp3)后点导入。",
                    style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.InkMuted,
                )
                PixelButton(
                    onClick = { imported = VoicePacks.importFromDownload(context) },
                    modifier = Modifier.fillMaxWidth().padding(top = OnTimeSpacing.sm),
                ) {
                    Text(
                        if (imported > 0) "已导入 $imported 个" else "导入语音包",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            PixelPanel(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.md)) {
                Text("关于", style = MaterialTheme.typography.titleMedium, color = OnTimeColors.Gold)
                Text(
                    "准时 v${versionName} · 本地提醒,无网络,无账号",
                    style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.InkWhite,
                )
                Text(
                    "开源:像素字体 Fusion Pixel(OFL)· 滑动确认基于 Brutus(MIT)· 素材转译 Kenney(CC0)",
                    style = MaterialTheme.typography.bodyMedium, color = OnTimeColors.InkMuted,
                    modifier = Modifier.padding(top = OnTimeSpacing.xs),
                )
            }

            PixelButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("返回", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
