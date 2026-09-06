package dev.evesis.ontime.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * OnTime Spacing v2(2026-09-06 Visual Redesign)
 * 尺度参考:Jenga(机械命名+语义层)·Carbon(2/4/8 的 2x Grid)· Apache-2.0。
 * 硬规则:布局代码禁止裸写 dp(2dp 像素描边等装饰例外,需行内注释);不够用时先扩语义层。
 *
 * 五层间距语义(§17):glyph(行高,在 Typography)→ compInt(组件内部)→ compGap(组件间)
 * → sectionGap(功能组间)→ gutter(屏缘)。
 */
object OnTimeSpacing {
    // 机械尺度(4dp 基准)
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
    val xxxxl = 64.dp

    // 语义层(唯一允许被布局引用的名字)
    val compInt = sm          // 组件内 padding(行内文字到容器)
    val compIntLg = lg        // 大组件内 padding(焦点卡/对话框)
    val compGap = md          // 同组组件间距(标题↔元数据之后到下一个控件)
    val rowGap = sm           // 列表行间距(v2 放宽到 lg 由列表自定:rows 用 lg)
    val sectionGap = xxxl     // 功能组到功能组(大呼吸,Visual Rhythm 主角)
    val sectionGapInner = xs  // 组眉标到组内容
    val gutter = xl           // 屏幕左右/底部边距(compact/medium)
    val gutterExpanded = xxxl // expanded(大平板/桌面)边距
    val heroTop = xxxl        // 首屏顶部到 Hero 的大呼吸
}

/** 内容宽度/自适应规则(§30-32;Window Size Class 语义,数值按官方断点) */
object OnTimeLayout {
    val compactMaxWidth = 600.dp    // <600: compact(手机竖屏)
    val mediumMaxWidth = 840.dp     // 600-840: medium(平板竖屏/手机横屏)
    // >=840: expanded(此平板 818dp 视配置/方向在 medium/expanded 间)

    /** 单列阅读宽(内容列上限;expanded 居中,两侧留白随窗口增长) */
    val contentMaxWidth = 560.dp
    /** 交互控件最大宽(按钮/滑动条不随屏无限拉宽) */
    val controlMaxWidth = 480.dp
}

/** 组件尺寸 Token(§34:仅交互件允许固定高,文本容器一律内容驱动) */
object OnTimeSizing {
    val minTouchTarget = 48.dp
    val buttonHeight = 56.dp
    val swipeThumb = 64.dp
    val fabSize = 64.dp           // 浮动新增/设置角块
    val hairline = 1.dp        // 细分隔线
    val borderFocused = 2.dp   // 焦点元素像素描边(唯一允许的粗框)
    val pixelUnit = 2.dp       // Pixel Unit(v11.2):边框/位移/阶梯的统一基础单位
}
