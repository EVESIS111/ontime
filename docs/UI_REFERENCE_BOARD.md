# UI_REFERENCE_BOARD.md — Visual Redesign 阶段参考板(2026-09-06)

> 规则:每个页面重构前必须能指向本板条目;禁止凭感觉设计。License 均实测。

## A. 业务结构参考(信息架构)
| Project | URL | License | 学习 | 禁止 | 适用 |
|---|---|---|---|---|---|
| yassineAbou/Clock | github.com/yassineAbou/Clock | Apache-2.0 | 闹钟列表信息层级(时间主导/重复规则为辅/开关右侧/编辑下沉)、Clear-all 交互 | 其 Material 默认脸 | Home/Editor |
| pepperonas/brutus | github.com/pepperonas/brutus | MIT | 编辑页分组、挑战式确认 | 品牌色 | Editor/Alert |

## B. 高品质视觉参考(排版/呼吸)
| Project | URL | License | 学习 | 禁止 | 适用 |
|---|---|---|---|---|---|
| **joelkanyi/jenga** | github.com/joelkanyi/jenga | Apache-2.0 | **Token 工程**(spacing 机械尺度+语义使用层;Display/Body 双字体;letterSpacing 分角色;LineHeightStyle 统一)——本轮核心方法参考 | Outfit 字体与其品牌 | 全局 Token |
| **gabrieldrn/carbon-compose** | github.com/gabrieldrn/carbon-compose | Apache-2.0 | 2x Grid 间距哲学(2/4/8 基准)、组件目录组织、"为什么每屏呼吸一致" | IBM Carbon 品牌灰白 | 全局 Token/组件 |
| nsh07/Tomato | github.com/nsh07/Tomato | GPL-3.0(**只读研究零复制**) | M3 Expressive 时间产品精致度基准:大时间主体、留白、层级、动效 | 任何源码 | 全局质量线 |
| compose-samples(Jetsnack) | github.com/android/compose-samples | Apache-2.0 | 设计系统拆组件、自定义排版 | 零食品牌视觉 | 组件层 |

## C. Pixel/Game UI 参考(高级感)
| Project | URL | License | 学习 | 禁止 | 适用 |
|---|---|---|---|---|---|
| Kenney Pixel UI | kenney.nl/assets/pixel-ui-pack | CC0 | 像素比例语言/边框克制(视觉比例参考,资产未嵌入) | 直接嵌资产变 Kenney Demo | 边框/角标 |
| theapache64/klokk | github.com/theapache64/klokk | Apache-2.0 | **一屏一物:时间=唯一主角**,大量负空间,KMP(compose 通用代码+桌面入口) | 其表盘具体绘制 | Home Hero/Alert |

## D. Adaptive/Cross-Platform 参考
| Project | URL | License | 学习 | 禁止 | 适用 |
|---|---|---|---|---|---|
| adaptive-apps-samples | github.com/android/adaptive-apps-samples | Apache-2.0 | canonical layout/list-detail、compact/medium/expanded 结构差异 | — | 全局布局 |
| compose-samples(Reply) | 同上 B | Apache-2.0 | 列表-详情自适应切换 | — | 平板布局 |

**合计 9 项**(5 强制 + 4 既有池精选)。视觉质量基准句:「完成度不能明显低于 Tomato;时间呈现学习 Klokk 的克制;Token 工程照 Jenga/Carbon」。
