#!/usr/bin/env bash
# restore-device-db.sh — 把本地备份安全推回设备(2026-09-06)
# 安全顺序:校验本地文件 → 装 debug → force-stop → stdin 流式推回(run-as 读不了 /sdbox)→ 回读校验。
set -euo pipefail

PKG="dev.evesis.ontime"
DB="databases/ontime.db"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
SRC="${1:?用法: restore-device-db.sh <备份.db>}"

[ -f "$SRC" ] || { echo "FAIL: $SRC not found"; exit 1; }
[ "$(head -c 15 "$SRC")" = "SQLite format 3" ] || { echo "FAIL: $SRC is not SQLite"; exit 1; }
SIZE=$(wc -c < "$SRC" | tr -d ' ')
[ "$SIZE" -ge 4096 ] || { echo "FAIL: $SRC only $SIZE bytes"; exit 1; }
ROWS=$(sqlite3 "$SRC" "SELECT COUNT(*) FROM reminders;" 2>/dev/null || { echo "FAIL: no reminders table"; exit 1; })
echo "source: $SRC ($SIZE bytes, reminders=$ROWS)"

"$ADB" get-state >/dev/null 2>&1 || { echo "FAIL: device not connected"; exit 1; }
DBG=$("$ADB" shell dumpsys package "$PKG" 2>/dev/null | grep -c "pkgFlags=.*DEBUGGABLE" || true)
if [ "$DBG" -eq 0 ]; then
  echo "FAIL: installed pkg is NOT debuggable;先装 debug 包(同签名 -r 保数据)"; exit 1
fi

"$ADB" shell am force-stop "$PKG"; sleep 1

# stdin 流式推回(run-as 无 /sdcard 读权限,scoped storage)
"$ADB" shell "run-as $PKG sh -c 'cat > $DB'" < "$SRC"
# WAL 安全:Room 默认 WAL,未 checkpoint 的数据在 -wal 里;备份若含伴生文件必须一并推回,
# 绝不能删(删除会丢 WAL 内未落盘数据)。残留 journal 属 truncate 模式旧库,推回的库自带其一致性状态。
SRC_BASE="$(basename "$SRC")"
BACKUP_DIR="$(dirname "$SRC")"
for suffix in "-wal" "-shm"; do
    EXTRA="$BACKUP_DIR/${SRC_BASE%.db}${suffix}"
    [ -f "$EXTRA" ] && "$ADB" shell "run-as $PKG sh -c 'cat > $DB${suffix}'" < "$EXTRA" && echo "pushed $DB${suffix}"
done

# 回读校验
"$ADB" shell "run-as $PKG cat $DB" > /tmp/restore-verify.db
VROWS=$(sqlite3 /tmp/restore-verify.db "SELECT COUNT(*) FROM reminders;" 2>/dev/null || echo "?")
[ "$VROWS" = "$ROWS" ] || { echo "FAIL: verify mismatch (device=$VROWS source=$ROWS)"; exit 1; }
echo "OK: restored and verified ($VROWS reminders)"
echo "提示:装回 release 用 adb install -r app-release.apk(数据保留)"
