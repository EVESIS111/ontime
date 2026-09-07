# 数据库维护（v12）

仅在约定设备窗口执行；本轮只用模拟 adb 验证，真机流程仍 UNVERIFIED。禁止 uninstall、清空 App 数据、直接写 filesDir 或使用 release 的 run-as。

## 前提与顺序

- 工具依赖本机 Bash、Python 3（标准库 sqlite3）、adb、shasum；不增加 App 依赖。
- 先 `adb devices -l` 确认 DBY2-W00；多设备时设置 `ANDROID_SERIAL`，脚本继承该变量。
- 检查 debug/release 与已安装版本签名相同；force-stop 后 `adb install -r` 装 debug。
- 运行 `tools/backup-device-db.sh`；脚本会再次 force-stop，检查 DEBUGGABLE，读取数据库目录，逐个比对传输字节数。
- 每次备份放进独立目录 `backups/ontime-时间-随机后缀/`。标准输出仅包含可用于恢复的绝对 DB 路径；提示写到标准错误。
- 完整原始文件集为 `ontime.db`、存在时的 `ontime.db-wal` / `ontime.db-shm` / `ontime.db-journal`，另有 SHA256.txt。原始文件不被本地 SQLite 打开修改。
- `validated.db` 是通过 SQLite backup 合并日志后的独立完整副本。仅修改它的另一个工作副本，不改备份；不要把原始 WAL 配给修改后的副本。
- 使用 `tools/restore-device-db.sh <工作副本.db>` 恢复。源校验失败不会联系设备。恢复脚本先自动备份设备当前库，再推完整快照，清空旧 WAL/SHM/journal，逐字节回读并校验 SQLite 完整性。
- 结束后 `adb install -r` 装回 release，核实 PACKAGE_REPLACED 自愈及下一次真实闹钟。工具不会代装 APK，也不会自动启动 App。

## 失败处理

- 空库、损坏库、非 OnTime v3/v4、缺表、旧式 `文件-wal` 歧义命名、目录读取失败、伴生文件传输失败或短读，都必须停止。
- 备份失败目录保留供诊断，不能恢复；只有脚本 OK 的完整备份或 validated.db 可用于恢复。
- 设备写入期间失败会保留恢复前备份路径，App 保持停止。不得直接打开 App；先修复连接，再从该安全备份重试恢复并通过校验，最后装回 release。
- 恢复是多次 adb 写入，**不具备跨文件原子性**；安全保障为停进程、预备份、失败停止和完整回读，不声称设备断线也自动回滚。
- 不自动删除备份，不上传，不记录语音或其他用户资产。

## 离线验证

`python3 tools/test-device-db.py`

测试使用临时 SQLite 和模拟 adb，只操作临时目录。覆盖 WAL-only 已提交数据、原始文件不变、无 WAL、同条数不同内容、空/损坏库、旧式伴生名、release 拒绝、目录/读取/写入失败、静默短读、旧 WAL 清空失败和回读不一致。通过不等于真实华为设备已验收。
