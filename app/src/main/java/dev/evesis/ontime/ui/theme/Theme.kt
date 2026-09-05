package dev.evesis.ontime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// OnTime 色板 v0(方向:深蓝星空 + 克制金;完整 Design Token 于 PHASE5 落地)
object OnTimeColors {
    val DeepBlue = Color(0xFF101B2E)      // 主背景:非纯黑的深蓝
    val DeepBlueHigh = Color(0xFF18263D)  // 层次提升面
    val Gold = Color(0xFFE3C068)          // 金色强调:细线/开关/关键数字
    val GoldDim = Color(0xFF9A8551)       // 金的弱化(禁用/辅助)
    val InkWhite = Color(0xFFEDE6D6)      // 暖白正文(非纯白)
    val InkMuted = Color(0xFF8E9BB3)      // 次要文字:蓝灰
    val VoiceCyan = Color(0xFF7FB4D8)     // 语音/角色信息点缀
}

private val OnTimeDarkScheme = darkColorScheme(
    primary = OnTimeColors.Gold,
    onPrimary = OnTimeColors.DeepBlue,
    secondary = OnTimeColors.VoiceCyan,
    onSecondary = OnTimeColors.DeepBlue,
    background = OnTimeColors.DeepBlue,
    onBackground = OnTimeColors.InkWhite,
    surface = OnTimeColors.DeepBlueHigh,
    onSurface = OnTimeColors.InkWhite,
    onSurfaceVariant = OnTimeColors.InkMuted,
    outline = OnTimeColors.GoldDim,
)

@Composable
fun OnTimeTheme(content: @Composable () -> Unit) {
    // 「准时」为游戏感夜空视觉,固定深色;亮色跟随无意义,保留 isSystemInDarkTheme 占位
    isSystemInDarkTheme()
    MaterialTheme(colorScheme = OnTimeDarkScheme, content = content)
}
