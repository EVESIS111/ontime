package dev.evesis.ontime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.evesis.ontime.ui.theme.*

/** Shared page rhythm; individual screens retain their state and actions. */
@Composable
fun WorkspacePage(title: String, subtitle: String, modifier: Modifier = Modifier,
    actions: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(OnTimeColors.DeepBlue).safeDrawingPadding()
        .padding(horizontal = LocalOnTimeAdaptive.current.gutter, vertical = OnTimeSpacing.xl)) {
        Row(Modifier.fillMaxWidth().padding(bottom = OnTimeSpacing.xl), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = OnTimeSpacing.lg)) {
                Text(title, style = OnTimePageTitle, color = OnTimeColors.InkWhite)
                if (subtitle.isNotBlank()) Text(subtitle, style = OnTimeSecondary,
                    color = OnTimeColors.InkMuted, modifier = Modifier.padding(top = OnTimeSpacing.xs))
            }
            actions()
        }
        content()
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
    Column(Modifier.fillMaxWidth().background(OnTimeColors.DeepBlueHigh)
        .padding(OnTimeSpacing.xl), verticalArrangement = Arrangement.spacedBy(OnTimeSpacing.lg)) {
        Column {
            Text(title, style = OnTimeFormLabel, color = OnTimeColors.InkWhite)
            if (subtitle.isNotEmpty()) Text(subtitle, style = OnTimeSecondary, color = OnTimeColors.InkMuted,
                modifier = Modifier.padding(top = OnTimeSpacing.xs))
        }
        content()
    }
}
