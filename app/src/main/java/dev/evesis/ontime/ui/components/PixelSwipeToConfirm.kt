package dev.evesis.ontime.ui.components

/*
 * 基于 pepperonas/brutus 的 SwipeToSnoozeButton.kt 移植(MIT License, © pepperonas,
 * 源:github.com/pepperonas/brutus app/src/main/java/com/pepperonas/brutus/ui/alarm/SwipeToSnoozeButton.kt)。
 * 改造:硬角像素皮肤(RectangleShape/金色/像素字体)、触发阈值 0.85→0.90(保持准时既有
 * ≥90% 滑动确认契约)、图标换字符、触觉走 LocalHapticFeedback。
 * 保留:drag 检测/Animatable/spring 回弹/脉冲提示/无障碍 customActions。
 */
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.OnTimeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 滑动确认(准时契约:划过 90% 触发;松手未达阈值弹回) */
@Composable
fun PixelSwipeToConfirm(
    hint: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val height = 80.dp
    val thumbSize = 64.dp
    val thumbPadding = 4.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val haptics = LocalHapticFeedback.current

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var triggered by remember { mutableStateOf(false) }

    val maxOffset = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
    val progress = if (maxOffset > 0f) (offsetX.value / maxOffset).coerceIn(0f, 1f) else 0f

    // 空闲脉冲提示
    val infinite = rememberInfiniteTransition(label = "swipeHint")
    val hintAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hintAlpha",
    )

    // 触发后复位(供复用;Alert 触发即 finish)
    LaunchedEffect(triggered) {
        if (triggered) {
            delay(400)
            offsetX.snapTo(0f)
            triggered = false
        }
    }

    fun fire() {
        if (!triggered) {
            triggered = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onConfirm()
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                contentDescription = hint
                customActions = listOf(CustomAccessibilityAction(hint) { fire(); true })
            }
            .background(OnTimeColors.DeepBlueHigh.copy(alpha = 0.9f))
            .border(2.dp, OnTimeColors.Gold)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
    ) {
        // 进度填充
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(OnTimeColors.Gold.copy(alpha = 0.10f + 0.25f * progress))
        )
        // 提示文字随拖动淡出
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = thumbSize + thumbPadding * 2)
                .alpha((1f - progress * 1.6f).coerceIn(0f, 1f)),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                hint,
                color = OnTimeColors.Gold.copy(alpha = hintAlpha),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        // 滑块
        Box(
            Modifier
                .padding(thumbPadding)
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .size(thumbSize)
                .background(OnTimeColors.Gold)
                .pointerInput(maxOffset) {
                    if (maxOffset <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= maxOffset * 0.90f) {   // 准时契约 ≥90%
                                    offsetX.animateTo(maxOffset, tween(150))
                                    fire()
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(0f, maxOffset)
                                offsetX.snapTo(next)
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("»", color = OnTimeColors.DeepBlue, style = MaterialTheme.typography.titleLarge)
        }
    }
}
