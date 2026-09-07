package dev.evesis.ontime.ui.settings

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.evesis.ontime.VoicePacks
import dev.evesis.ontime.data.AlarmHealth
import dev.evesis.ontime.data.AlarmHealthProbe
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.components.SectionHeader
import dev.evesis.ontime.ui.theme.OnTimeButtonLabel
import dev.evesis.ontime.ui.theme.OnTimeBody
import dev.evesis.ontime.ui.theme.OnTimeCaption
import dev.evesis.ontime.ui.theme.OnTimeMetadata
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeScreenTitle
import dev.evesis.ontime.ui.theme.OnTimeSecondary
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/*
 * Settings v2(§29):眉标分组+hairline+行式布局;像素装饰仅按钮一处,无 RPG 弹框阵列。
 */

@Composable
fun SettingsScreen(onBack: () -> Unit, onCatalog: () -> Unit = {}) {
    val context = LocalContext.current
    var imported by remember { mutableStateOf(-1) }
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?" }
        catch (e: Exception) { "?" }
    }
    val gitSha = remember { dev.evesis.ontime.BuildConfig.GIT_SHA }

    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                top = OnTimeSpacing.xxl,
                bottom = OnTimeSpacing.gutter,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = OnTimeSpacing.xl)) {
            Text("设置", style = OnTimeScreenTitle, color = OnTimeColors.InkWhite)

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))

            // ── 运行状态 ──
            SectionHeader("运行状态")
            HealthRow(context)

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))

            // ── 语音包 ──
            SectionHeader("语音包")
            Text(
                "把语音文件放入 下载/ontime-voices/(命名:音色id__40位哈希.mp3),然后导入。",
                style = OnTimeBody,
                color = OnTimeColors.InkMuted,
            )
            PixelButton(
                onClick = { imported = VoicePacks.importFromDownload(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OnTimeSpacing.md),
            ) {
                Text(
                    when { imported > 0 -> "已导入 $imported 个"; imported == 0 -> "未找到可导入的语音包"; else -> "导入语音包" },
                    style = OnTimeButtonLabel,
                )
            }

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))

            // ── 关于 ──
            SectionHeader("关于")
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

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("开发")
            QuietButton(onClick = onCatalog, text = "组件目录 →")
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            QuietButton(
                onClick = onBack,
                text = "← 返回",
                emphasize = true,
                modifier = Modifier.align(Alignment.Start),
            )
        }
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
