package dev.evesis.ontime.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import dev.evesis.ontime.R

/*
 * OnTime Typography v2(2026-09-06 Visual Redesign)
 * 方法参考:jenga(双 typeface 架构+分角色 letterSpacing+统一 LineHeightStyle)· Apache-2.0。
 *
 * 双字体职责(方案 B,真机对比后确认):
 * - Pixel(Fusion Pixel 子集)= 品牌/身份:时间数字、屏标题、章节标题、按钮、角色台词
 * - Sans(系统默认)= 可读性:正文、说明、Metadata、Caption —— 中文正文像素字拥挤,换 sans 后
 *   像素标题反而更突出(Jenga display/body 分工同理念)。
 *
 * 硬规则:字号只允许出现在本文件;Screen 内禁止 fontSize=,角色不足时先扩表。
 * 像素字保持 10 的倍数(设计网格整数倍,§15 清晰度);sans 不受此限。
 */

val OnTimePixelFont = FontFamily(Font(R.font.ontime_pixel_zh))
val OnTimeSansFont = FontFamily.Default

private val pixelLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

// ── Pixel 角色(身份层)──────────────────────────────────────────
/** 首页/Alert 的 Hero 时间数字(如 13:07),页面唯一最大元素 */
val OnTimeHeroTime = TextStyle(
    fontFamily = OnTimePixelFont, fontWeight = FontWeight.Normal,
    fontSize = 80.sp, lineHeight = 100.sp, letterSpacing = 2.sp,   // 数字放宽字距更从容
    lineHeightStyle = pixelLineHeightStyle,
)
/** 屏级大标题(「准时」、Alert 的提醒名) */
val OnTimeHeroTitle = TextStyle(
    fontFamily = OnTimePixelFont, fontWeight = FontWeight.Normal,
    fontSize = 50.sp, lineHeight = 64.sp, letterSpacing = 4.sp,    // 中文标题加字距呼吸
    lineHeightStyle = pixelLineHeightStyle,
)
/** 页面标题(编辑/设置) */
val OnTimeScreenTitle = TextStyle(
    fontFamily = OnTimePixelFont, fontWeight = FontWeight.Normal,
    fontSize = 40.sp, lineHeight = 52.sp, letterSpacing = 3.sp,
    lineHeightStyle = pixelLineHeightStyle,
)
/** 分组标题(编辑页 TIME/SCHEDULE…;提醒行主标题) */
val OnTimeSectionTitle = TextStyle(
    fontFamily = OnTimePixelFont, fontWeight = FontWeight.Normal,
    fontSize = 30.sp, lineHeight = 42.sp, letterSpacing = 2.sp,
    lineHeightStyle = pixelLineHeightStyle,
)
/** 按钮文字 */
val OnTimeButtonLabel = TextStyle(
    fontFamily = OnTimePixelFont, fontWeight = FontWeight.Normal,
    fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 3.sp,
    lineHeightStyle = pixelLineHeightStyle,
)

// ── Sans 角色(可读层)────────────────────────────────────────────
/** 重要正文(Alert 台词、编辑页当前值) */
val OnTimeBodyLarge = TextStyle(
    fontFamily = OnTimeSansFont, fontWeight = FontWeight.Medium,
    fontSize = 18.sp, lineHeight = 30.sp, letterSpacing = 0.4.sp,
)
/** 常规正文(描述、设置说明) */
val OnTimeBody = TextStyle(
    fontFamily = OnTimeSansFont, fontWeight = FontWeight.Normal,
    fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp,
)
/** 次要正文(辅助说明,容忍两行以上) */
val OnTimeSecondary = TextStyle(
    fontFamily = OnTimeSansFont, fontWeight = FontWeight.Normal,
    fontSize = 13.sp, lineHeight = 21.sp, letterSpacing = 0.3.sp,
)
/** 行内元数据(下次时间/重复规则) */
val OnTimeMetadata = TextStyle(
    fontFamily = OnTimeSansFont, fontWeight = FontWeight.Medium,
    fontSize = 13.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp,
)
/** 章节眉标(小号金色,配 hairline 使用) */
val OnTimeEyebrow = TextStyle(
    fontFamily = OnTimeSansFont, fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 2.5.sp,  // 全大写风格宽字距
)
/** Caption(版权/许可/时间戳) */
val OnTimeCaption = TextStyle(
    fontFamily = OnTimeSansFont, fontWeight = FontWeight.Normal,
    fontSize = 11.sp, lineHeight = 17.sp, letterSpacing = 0.3.sp,
)

/** M3 Typography 映射(Material 组件内部消费;项目自有代码直接用上面的角色) */
val OnTimeTypography = Typography(
    displaySmall = OnTimeHeroTime,
    headlineMedium = OnTimeHeroTitle,
    titleLarge = OnTimeScreenTitle,
    titleMedium = OnTimeSectionTitle,
    titleSmall = OnTimeButtonLabel,
    bodyLarge = OnTimeBodyLarge,
    bodyMedium = OnTimeBody,
    bodySmall = OnTimeSecondary,
    labelLarge = OnTimeButtonLabel,
    labelMedium = OnTimeMetadata,
    labelSmall = OnTimeCaption,
)
