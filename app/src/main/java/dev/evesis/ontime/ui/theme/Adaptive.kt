package dev.evesis.ontime.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * OnTimeAdaptiveSpec(v11.3):集中式窗口适配状态(§31/§82)。
 * 测量来源=BoxWithConstraints 可用空间(App Window 真实值,split-screen/桌面窗口正确;
 * commonMain API,CMP 兼容)。页面禁止各自判宽,全部读此 Spec。
 * 断点=官方 WindowSizeClass 数值(600/840)。
 */
data class OnTimeAdaptiveSpec(
    val isCompact: Boolean,      // <600dp 手机竖屏/窄窗
    val isMedium: Boolean,       // 600-840
    val isExpanded: Boolean,     // ≥840 大平板/桌面
    val isLandscape: Boolean,    // 宽>高
    val isShortHeight: Boolean,  // 高度 <480dp(横屏矮窗)
    val heroTimeSp: Int,         // Hero 时钟三档(离散,§40)
    val gutter: Dp,
    val maxListWidth: Dp,
    val maxFormWidth: Dp,
) {
    val twoPane: Boolean get() = isExpanded && isLandscape
    companion object {
        fun from(width: Dp, height: Dp): OnTimeAdaptiveSpec {
            val compact = width < 600.dp
            val medium = !compact && width < 840.dp
            val expanded = width >= 840.dp
            val landscape = width > height
            val shortHeight = height < 480.dp
            return OnTimeAdaptiveSpec(
                isCompact = compact,
                isMedium = medium,
                isExpanded = expanded,
                isLandscape = landscape,
                isShortHeight = shortHeight,
                heroTimeSp = when {
                    shortHeight -> 72     // 横屏矮窗:时钟降档防纵向爆(§34/§78)
                    compact -> 88         // 手机竖屏
                    medium -> 104         // 小平板
                    else -> 120           // 大平板/桌面
                },
                gutter = when {
                    compact -> 20.dp
                    medium -> 24.dp
                    else -> 48.dp
                },
                maxListWidth = 640.dp,
                maxFormWidth = 560.dp,
            )
        }
    }
}

/** 唯一 Composition Mode(§40-42:结构选择;Spec 只管尺寸 token;一个窗口只落一个 mode) */
enum class OnTimeLayoutMode { SINGLE_PANE, DASHBOARD }

val OnTimeAdaptiveSpec.layoutMode: OnTimeLayoutMode
    get() = if (isExpanded && isLandscape) OnTimeLayoutMode.DASHBOARD else OnTimeLayoutMode.SINGLE_PANE

val LocalOnTimeAdaptive = staticCompositionLocalOf {
    OnTimeAdaptiveSpec(false, false, true, false, false, 120, 48.dp, 640.dp, 560.dp)
}

/** 在布局树根部调用一次(读真实可用空间);子树经 LocalOnTimeAdaptive 消费 */
@Composable
fun OnTimeAdaptive(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val spec = OnTimeAdaptiveSpec.from(maxWidth, maxHeight)
        androidx.compose.runtime.CompositionLocalProvider(LocalOnTimeAdaptive provides spec) {
            content()
        }
    }
}
