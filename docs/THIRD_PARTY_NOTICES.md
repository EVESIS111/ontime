# THIRD_PARTY_NOTICES.md — 第三方使用与许可(2026-09-06 审计)

| 内容 | 来源 | 许可 | 使用方式 | 本项目位置 |
|---|---|---|---|---|
| 项目架构基准/版本目录 | android/architecture-templates(Google) | Apache-2.0 | 模式参照(依赖版本/结构) | gradle/libs.versions.toml |
| Compose/Material3/lifecycle/navigation/Room | AndroidX(Google) | Apache-2.0 | 直接依赖 | app/build.gradle.kts |
| 滑动确认手势实现 | pepperonas/brutus SwipeToSnoozeButton.kt | MIT(© pepperonas) | **源码移植**(版权声明保留于文件头;阈值 0.85→0.90 适配准时契约;硬角像素换肤) | ui/components/PixelSwipeToConfirm.kt |
| 像素中文字体 | TakWolf/fusion-pixel-font v2026.09.01(10px monospaced zh_hans) | OFL-1.1 | pyftsubset 子集化(3.9MB→659KB;GB2312 一级+ASCII+界面文案) | res/font/ontime_pixel_zh.ttf + assets/licences/OFL-fusion-pixel.txt |
| Fusion Pixel 上游字体 | ark-pixel/boutique-bitmap/galmuri 等 | 各自 OFL/许可 | 随上游一并适用 | 见 OFL-fusion-pixel.txt 附注 |
| 像素 UI 视觉语言参考 | Kenney Pixel UI Pack | CC0 | 视觉比例/边框语言研究(未直接嵌入资产) | docs/UI_REFERENCE_MAP.md |

## 合规说明
- GPL 项目(reReminder/FossifyClock)仅阅读研究,零代码复制。
- MIT 移植文件保留原始版权与许可声明(见文件头注释)。
- OFL 字体:subsets 属于 OFL 允许的修改再分发;保留许可文件随 APK 分发(assets/licences/)。
- App 自身未声明开源许可(自用);若未来公开发布,本文件与 licenses 目录需随包提供。
