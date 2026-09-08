package dev.evesis.ontime.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.evesis.ontime.ui.theme.*

/** Stepped, hard-edged housing. Geometry only; no fake interactive controls. */
fun Modifier.consoleFrame(face: Color = OnTimeColors.ConsoleBody, inset: Boolean = false) = drawBehind {
    val unit = 4.dp.toPx()
    val light = if (inset) OnTimeColors.ConsoleShadow else OnTimeColors.ConsoleEdge
    val dark = if (inset) OnTimeColors.ConsoleEdge else OnTimeColors.ConsoleShadow
    drawRect(face, Offset(unit, unit), Size(size.width - unit * 2, size.height - unit * 2))
    drawRect(light, Offset(unit * 2, 0f), Size(size.width - unit * 4, unit))
    drawRect(light, Offset(0f, unit * 2), Size(unit, size.height - unit * 4))
    drawRect(dark, Offset(unit * 2, size.height - unit), Size(size.width - unit * 4, unit))
    drawRect(dark, Offset(size.width - unit, unit * 2), Size(unit, size.height - unit * 4))
    listOf(Offset(unit, unit), Offset(size.width - unit * 2, unit),
        Offset(unit, size.height - unit * 2), Offset(size.width - unit * 2, size.height - unit * 2)).forEach {
        drawRect(if (inset) dark else light, it, Size(unit, unit))
    }
}

@Composable
fun WorkspacePage(title: String, subtitle: String, modifier: Modifier = Modifier,
    actions: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(OnTimeColors.ConsoleShadow).safeDrawingPadding().padding(OnTimeSpacing.sm)) {
        Column(Modifier.fillMaxSize().consoleFrame().padding(OnTimeSpacing.lg)) {
            Row(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.lg), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.md)) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = OnTimePageTitle, color = OnTimeColors.InkWhite)
                    if (subtitle.isNotBlank()) Text(subtitle, style = OnTimeSecondary,
                        color = OnTimeColors.InkMuted, modifier = Modifier.padding(top = OnTimeSpacing.xs))
                }
                actions()
            }
            Column(Modifier.weight(1f).fillMaxWidth().consoleFrame(OnTimeColors.DeepBlue, inset = true)
                .padding(OnTimeSpacing.lg)) { content() }
            Row(Modifier.fillMaxWidth().padding(top = OnTimeSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Text("ON TIME", style = OnTimeMetadata, color = OnTimeColors.InkWhite, modifier = Modifier.weight(1f))
                Canvas(Modifier.width(OnTimeSpacing.xxxxl).height(OnTimeSpacing.md)) {
                    val bar = 4.dp.toPx()
                    repeat(6) { drawRect(OnTimeColors.ConsoleShadow, Offset(it * bar * 2, 0f), Size(bar, size.height)) }
                }
            }
        }
    }
}

@Composable
fun WorkspaceColumns(modifier: Modifier = Modifier, leadingWeight: Float = 1f,
    leading: @Composable () -> Unit, trailing: @Composable () -> Unit) {
    when (LocalOnTimeAdaptive.current.layoutMode) {
        OnTimeLayoutMode.DASHBOARD -> Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.xl)) {
            Column(Modifier.weight(leadingWeight).fillMaxHeight().verticalScroll(rememberScrollState())) { leading() }
            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) { trailing() }
        }
        OnTimeLayoutMode.SINGLE_PANE -> Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.xl)) { leading(); trailing() }
    }
}

@Composable
fun WorkspacePanel(title: String, subtitle: String = "", content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().consoleFrame(OnTimeColors.DeepBlueHigh, inset = true)
        .padding(OnTimeSpacing.lg), verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.lg)) {
        Column {
            Text("▸ $title", style = OnTimeFormLabel, color = OnTimeColors.Gold)
            if (subtitle.isNotEmpty()) Text(subtitle, style = OnTimeSecondary, color = OnTimeColors.InkMuted,
                modifier = Modifier.padding(top = OnTimeSpacing.xs))
        }
        content()
    }
}
