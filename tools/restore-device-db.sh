#!/usr/bin/env bash
# Normalize DB+WAL locally, back up current device data, restore and compare bytes.
set -euo pipefail
PKG="dev.evesis.ontime"
DB="databases/ontime.db"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
SRC="${1:?Usage: restore-device-db.sh <backup.db>}"
TOOLS_DIR="$(cd "$(dirname "$0")" && pwd)"
WORK=$(mktemp -d "${TMPDIR:-/tmp}/ontime-restore-XXXXXX")
cleanup() {
    for name in snapshot.db verify.db checked.db; do
        for suffix in '' -wal -shm -journal; do rm -f "$WORK/$name$suffix"; done
    done
    rm -f "$WORK/sidecar"
    rmdir "$WORK"
}
trap cleanup EXIT
# No device access or writes until the source is fully validated.
python3 "$TOOLS_DIR/db-snapshot.py" "$SRC" "$WORK/snapshot.db"
SAFETY=$("$TOOLS_DIR/backup-device-db.sh" "${ONTIME_BACKUP_DIR:-$TOOLS_DIR/../backups}")
echo "Safety backup: $SAFETY"
trap 'echo "FAIL: device restore incomplete; app remains stopped. Safety backup: $SAFETY" >&2' ERR
"$ADB" shell "run-as $PKG sh -c 'cat > $DB'" < "$WORK/snapshot.db"
# Snapshot already contains committed WAL data. Empty old journals cannot replay
# stale pages over it; the pre-restore capture above preserves the original set.
for suffix in -wal -shm -journal; do
    "$ADB" shell "run-as $PKG sh -c 'cat > $DB$suffix'" < /dev/null
    "$ADB" exec-out run-as "$PKG" cat "$DB$suffix" > "$WORK/sidecar"
    [ ! -s "$WORK/sidecar" ] || { echo "FAIL: stale $suffix remains" >&2; exit 1; }
done
"$ADB" exec-out run-as "$PKG" cat "$DB" > "$WORK/verify.db"
cmp "$WORK/snapshot.db" "$WORK/verify.db"
python3 "$TOOLS_DIR/db-snapshot.py" "$WORK/verify.db" "$WORK/checked.db"
trap - ERR
echo "OK: restored, byte-for-byte verified, integrity_check passed"
echo "Reinstall release with adb install -r, then verify alarm recovery."
