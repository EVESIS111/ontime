#!/usr/bin/env bash
# backup-device-db.sh — 设备数据库安全备份(2026-09-06,依据 Stable Foundation 指令 §19)
# 教训背景:09-05 曾因 release 包 run-as 静默失败 + 空文件推回,把设备 DB 覆盖为 0 字节。
# 本脚本固化安全顺序;任何真实 DB 写操作前必须先跑它。
set -euo pipefail

PKG="dev.evesis.ontime"
DB="databases/ontime.db"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
OUT_DIR="${1:-$HOME/Desktop/准时App项目/backups}"

# 1. 设备在线
"$ADB" get-state >/dev/null 2>&1 || { echo "FAIL: device not connected"; exit 1; }

# 2. 必须是 debug 包(run-as 才可用);release 包上 run-as 会静默失败——历史事故根因
DBG=$("$ADB" shell dumpsys package "$PKG" 2>/dev/null | grep -c "pkgFlags=.*DEBUGGABLE" || true)
if [ "$DBG" -eq 0 ]; then
  echo "FAIL: installed pkg is NOT debuggable — run-as would silently return nothing."
  echo "      先 adb install -r app/build/outputs/apk/debug/app-debug.apk(同签名,-r 保数据)"
  exit 1
fi

# 3. 停 App(避免连接持有/WAL 未落盘),再拉取
"$ADB" shell am force-stop "$PKG"; sleep 1

TS=$(date +%Y%m%d-%H%M%S)
mkdir -p "$OUT_DIR"
OUT="$OUT_DIR/ontime-$TS.db"

"$ADB" shell "run-as $PKG cat $DB" > "$OUT"
SIZE=$(wc -c < "$OUT" | tr -d ' ')

# 4. 拉取结果必须校验(空文件/非 SQLite 头都是失败)
if [ "$SIZE" -lt 4096 ]; then
  echo "FAIL: pulled file only $SIZE bytes (suspect run-as failure)"; rm -f "$OUT"; exit 1
fi
if [ "$(head -c 15 "$OUT")" != "SQLite format 3" ]; then
  echo "FAIL: not a SQLite file"; rm -f "$OUT"; exit 1
fi

# 5. 附带 journal/wal(存在时)
for extra in "$DB-journal" "$DB-wal" "$DB-shm"; do
  if "$ADB" shell "run-as $PKG ls $extra" >/dev/null 2>&1; then
    "$ADB" shell "run-as $PKG cat $extra" > "$OUT_DIR/$(basename $extra)-$TS" 2>/dev/null || true
  fi
done

ROWS=$(sqlite3 "$OUT" "SELECT COUNT(*) FROM reminders;" 2>/dev/null || echo "?")
SCHEMA=$(sqlite3 "$OUT" "PRAGMA user_version;" 2>/dev/null || echo "?")
echo "OK: $OUT ($SIZE bytes, reminders=$ROWS, schema=v$SCHEMA)"
echo "sha256: $(shasum -a 256 "$OUT" | cut -d' ' -f1)"
