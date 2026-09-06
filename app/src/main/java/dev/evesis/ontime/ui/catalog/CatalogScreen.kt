package dev.evesis.ontime.ui.catalog

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.evesis.ontime.ui.components.FocusPanel
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelStepper
import dev.evesis.ontime.ui.components.PixelTextField
import dev.evesis.ontime.ui.components.PixelToggle
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.components.QuietSurface
import dev.evesis.ontime.ui.components.SectionHeader
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeScreenTitle
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/** OnTimePixelKit 组件目录(§56:单组件风格统一先行;真机截图验收) */
@Composable
fun CatalogScreen(onBack: () -> Unit) {
    var t1 by remember { mutableStateOf(true) }
    var t2 by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(9) }
    var text by remember { mutableStateOf("示例标题") }

    Column(
        Modifier
            .fillMaxSize()
            .background(OnTimeColors.DeepBlue)
            .verticalScroll(rememberScrollState())
            .padding(top = OnTimeSpacing.xxl, bottom = OnTimeSpacing.gutter),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = OnTimeSpacing.xl)) {
            Text("组件目录", style = OnTimeScreenTitle, color = OnTimeColors.InkWhite)

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("BUTTON")
            PixelButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("主要动作", style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel)
            }
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            QuietButton(onClick = {}, text = "次级动作")
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            PixelButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("禁用态", style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel)
            }

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("TOGGLE")
            Row(horizontalArrangement = Arrangement.spacedBy(OnTimeSpacing.xl)) {
                PixelToggle(checked = t1, onCheckedChange = { t1 = it })
                PixelToggle(checked = t2, onCheckedChange = { t2 = it })
                Text("左=开 · 右=关(点击即时)", style = dev.evesis.ontime.ui.theme.OnTimeSecondary,
                    color = OnTimeColors.InkMuted, modifier = Modifier.padding(top = OnTimeSpacing.sm))
            }

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("SURFACE")
            FocusPanel(Modifier.fillMaxWidth()) {
                Text("焦点面板 FocusPanel", style = dev.evesis.ontime.ui.theme.OnTimeBody,
                    color = OnTimeColors.InkWhite)
            }
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            QuietSurface(Modifier.fillMaxWidth()) {
                Text("静默面 QuietSurface", style = dev.evesis.ontime.ui.theme.OnTimeBody,
                    color = OnTimeColors.InkWhite)
            }

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("STEPPER / INPUT")
            PixelStepper(
                label = "小时", value = "%02d".format(step),
                onPrev = { step = (step + 23) % 24 }, onNext = { step = (step + 1) % 24 },
            )
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            PixelTextField(label = "文本输入", value = text, onValueChange = { text = it })

            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            QuietButton(onClick = onBack, text = "← 返回", emphasize = true)
        }
    }
}

@Preview(name = "Tablet", widthDp = 818, heightDp = 1200)
@Composable
private fun CatalogPreview() {
    OnTimeTheme { CatalogScreen(onBack = {}) }
}
