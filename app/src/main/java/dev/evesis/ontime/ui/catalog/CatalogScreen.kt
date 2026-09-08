package dev.evesis.ontime.ui.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.evesis.ontime.ui.components.WorkspacePage
import androidx.compose.foundation.layout.widthIn
import dev.evesis.ontime.ui.theme.OnTimeLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.evesis.ontime.ui.components.FocusPanel
import dev.evesis.ontime.ui.components.PixelButton
import dev.evesis.ontime.ui.components.PixelGlyph
import dev.evesis.ontime.ui.components.PixelStepper
import dev.evesis.ontime.ui.components.PixelTextField
import dev.evesis.ontime.ui.components.PixelToggle
import dev.evesis.ontime.ui.components.QuietButton
import dev.evesis.ontime.ui.components.QuietSurface
import dev.evesis.ontime.ui.components.SectionHeader
import dev.evesis.ontime.ui.theme.OnTimeColors
import dev.evesis.ontime.ui.theme.OnTimeSpacing
import dev.evesis.ontime.ui.theme.OnTimeTheme

/** OnTimePixelKit 组件目录(§56:单组件风格统一先行;真机截图验收) */
@Composable
fun CatalogScreen(onBack: () -> Unit) {
    var t1 by remember { mutableStateOf(true) }
    var t2 by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(9) }
    var text by remember { mutableStateOf("示例标题") }

    WorkspacePage("组件目录", "全应用的视觉与交互基线", actions = { QuietButton(onClick = onBack, text = "返回") }) {
        Column(Modifier.weight(1f).widthIn(max = OnTimeLayout.editorMaxWidth).fillMaxWidth().verticalScroll(rememberScrollState())) {
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

            // ── 状态矩阵(§51)+ 命中框可视化(§50:虚线=48dp hit box;视觉=内部图标/开关)──
            Spacer(Modifier.padding(top = OnTimeSpacing.sectionGap))
            SectionHeader("STATE MATRIX")
            Text("按下态:常态金框,按下=金底+内容下沉(截图为静态展示)", style = dev.evesis.ontime.ui.theme.OnTimeSecondary, color = OnTimeColors.InkMuted, modifier = Modifier.padding(bottom = OnTimeSpacing.sm))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OnTimeSpacing.lg), modifier = Modifier.fillMaxWidth()) {
                dev.evesis.ontime.ui.components.PixelButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("常态", style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(dev.evesis.ontime.ui.theme.OnTimeSizing.buttonHeight)
                        .background(OnTimeColors.Gold),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("按下", style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel, color = OnTimeColors.DeepBlue)
                }
                dev.evesis.ontime.ui.components.PixelButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                    Text("禁用", style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel)
                }
            }
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OnTimeSpacing.lg), modifier = Modifier.fillMaxWidth()) {
                Text("选中(金底):", style = dev.evesis.ontime.ui.theme.OnTimeMetadata, color = OnTimeColors.InkMuted, modifier = Modifier.padding(top = OnTimeSpacing.sm))
                Text(
                    "每天",
                    style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel,
                    color = OnTimeColors.DeepBlue,
                    modifier = Modifier
                        .background(OnTimeColors.Gold)
                        .border(2.dp, OnTimeColors.InkWhite)
                        .padding(horizontal = OnTimeSpacing.xl, vertical = OnTimeSpacing.md),
                )
                Text(
                    "试听中",
                    style = dev.evesis.ontime.ui.theme.OnTimeButtonLabel,
                    color = OnTimeColors.Gold,
                    modifier = Modifier.padding(top = OnTimeSpacing.sm),
                )
                Text("不可用", style = dev.evesis.ontime.ui.theme.OnTimeMetadata, color = OnTimeColors.Gold, modifier = Modifier.padding(top = OnTimeSpacing.sm))
            }
            Spacer(Modifier.padding(top = OnTimeSpacing.md))
            SectionHeader("HIT BOX")
            Text("小图标外有 48dp 命中区(此页以描边示意):", style = dev.evesis.ontime.ui.theme.OnTimeSecondary, color = OnTimeColors.InkMuted)
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OnTimeSpacing.xl), modifier = Modifier.padding(top = OnTimeSpacing.sm)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .border(1.dp, OnTimeColors.VoiceCyan.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) { dev.evesis.ontime.ui.components.PixelIcon(glyph = PixelGlyph.PREV) }
                Box(
                    Modifier
                        .size(48.dp)
                        .border(1.dp, OnTimeColors.VoiceCyan.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) { dev.evesis.ontime.ui.components.PixelIcon(glyph = PixelGlyph.PLAY) }
                Box(
                    Modifier
                        .size(48.dp)
                        .border(1.dp, OnTimeColors.VoiceCyan.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) { dev.evesis.ontime.ui.components.PixelIcon(glyph = PixelGlyph.SETTINGS, sizeDp = 22) }
            }

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
