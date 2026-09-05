package dev.evesis.ontime.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.evesis.ontime.R

/**
 * 像素字体:Fusion Pixel 10px monospaced 子集(3755 常用汉字+ASCII+界面文案,pyftsubset 生成,659KB)。
 * 规则(任务书 §15):字号只用 10 的倍数(设计网格 10px 的整数倍),禁止 15sp 等半像素模糊尺寸;
 * 层级 = 40/30/20 三级 + 颜色区分。生僻字(用户输入)自动回退系统字体。
 * 许可:OFL-1.1(assets/licences/OFL-fusion-pixel.txt;源 github.com/TakWolf/fusion-pixel-font v2026.09.01)
 */
val OnTimePixelFont = FontFamily(Font(R.font.ontime_pixel_zh))

val OnTimeTypography = Typography(
    displaySmall = TextStyle(fontFamily = OnTimePixelFont, fontSize = 40.sp, lineHeight = 50.sp),
    headlineMedium = TextStyle(fontFamily = OnTimePixelFont, fontSize = 40.sp, lineHeight = 50.sp),
    titleLarge = TextStyle(fontFamily = OnTimePixelFont, fontSize = 30.sp, lineHeight = 40.sp),
    titleMedium = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
    bodyLarge = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
    bodySmall = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
    labelLarge = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
    labelMedium = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
    labelSmall = TextStyle(fontFamily = OnTimePixelFont, fontSize = 20.sp, lineHeight = 30.sp),
)

/** 间距 Token:4dp 网格;布局数字一律引用此处,禁止裸调像素 */
object OnTimeSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val contentMaxWidth = 600.dp   // 平板 expanded 下内容列最大宽度(单列阅读宽)
}
