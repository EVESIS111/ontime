package dev.evesis.ontime.ui.components

/*
 * 基于 pepperonas/brutus 的 SwipeToSnoozeButton.kt 移植(MIT License, © pepperonas,
 * 源:github.com/pepperonas/brutus app/src/main/java/com/pepperonas/brutus/ui/alarm/SwipeToSnoozeButton.kt)。
 * v11.2 Instant Pixel 修订:删除脉冲提示/spring 回弹/150ms tween——跟手保留(snapTo),
 * 释放未达阈值立即归位(snapTo 0),触发立即回调;阈值保持准时 ≥90% 契约。
 * 保留:drag 检测/Animatable(仅作 state 容器)/无障碍 customActions。
 */
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.evesis.ontime.ui.theme.OnTimeColors

/** 像素滑动条:Cell 式进度(每 1/8 一格,离散无插值);划过 90% 立即触发 */
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

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val offsetPx = remember { mutableFloatStateOf(0f) }   // 直接 state,无 Animatable 插值
    val scope = rememberCoroutineScope()
    var triggered by remember { mutableStateOf(false) }

    val maxOffset = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
    val progress = if (maxOffset > 0f) (offsetPx.floatValue / maxOffset).coerceIn(0f, 1f) else 0f
    val cells = 8
    val litCells = (progress * cells).toInt()

    fun fire() {
        if (!triggered) {
            triggered = true
            onConfirm()
        }
    }

    LaunchedEffect(triggered) {
        if (triggered) { kotlinx.coroutines.delay(300); offsetPx.floatValue = 0f; triggered = false }
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
            .border(2.dp, OnTimeColors.Gold, RectangleShape)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
    ) {
        // Cell 进度(离散方块,像老游戏体力条)
        Row(Modifier.fillMaxSize()) {
            repeat(cells) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .then(
                                if (i < litCells) Modifier.background(OnTimeColors.Gold.copy(alpha = 0.28f))
                                else Modifier
                            ),
                    )
                }
            }
        }
        // 提示文字(随拖动淡出,alpha 由进度直接驱动,无动画)
        Text(
            hint,
            color = OnTimeColors.Gold.copy(alpha = (1f - progress * 1.6f).coerceIn(0f, 1f)),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = thumbSize + thumbPadding * 2),
        )
        // 滑块(跟手;释放立即归位)
        Box(
            Modifier
                .padding(thumbPadding)
                .offset { IntOffset(offsetPx.floatValue.toInt(), 0) }
                .size(thumbSize)
                .background(OnTimeColors.Gold)
                .pointerInput(maxOffset) {
                    if (maxOffset <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetPx.floatValue >= maxOffset * 0.90f) {
                                offsetPx.floatValue = maxOffset
                                fire()
                            } else {
                                offsetPx.floatValue = 0f     // 立即归位,零动画
                            }
                        },
                        onDragCancel = { offsetPx.floatValue = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetPx.floatValue = (offsetPx.floatValue + dragAmount).coerceIn(0f, maxOffset)
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("»", color = OnTimeColors.DeepBlue, style = MaterialTheme.typography.titleLarge)
        }
    }
}
