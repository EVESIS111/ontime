# TESTING.md

## 现有
- ScheduleEngineTest(JUnit4,纯 JVM):randomCrossCheck1000(1000 组随机×异构 oracle 对拍:逐分钟暴力步进 vs 纯数学)+4 专项(窗口/免打扰/一次性/边界)。全绿。
- 真机验收:60s ONCE 插桩法(见 DEVICE_DBY2-W00.md)。

## 迁移期规则
改动前先 characterization test(旧输入→旧输出);新实现必须输出一致。
新增重点(任务书 §45):00:00/08:30 边界、跨午夜窗口、月末/闰年/时区、interval rollover、disabled、ONCE 过期、snooze。
UI Test 只测关键流(Home 显示/CRUD/开关/Snooze/滑动/设置持久化),不刷覆盖率。
