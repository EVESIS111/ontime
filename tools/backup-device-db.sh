#!/usr/bin/env bash
# Pull the complete stopped DB file set; validate a copy without checkpointing it.
set -euo pipefail
PKG="dev.evesis.ontime"
DB="databases/ontime.db"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
OUT_DIR="${1:-$HOME/Desktop/准时App项目/backups}"
TOOLS_DIR="$(cd "$(dirname "$0")" && pwd)"

"$ADB" get-state >/dev/null
FLAGS=$("$ADB" shell dumpsys package "$PKG")
[[ "$FLAGS" =~ pkgFlags=.*DEBUGGABLE ]] || { echo "FAIL: install same-signature debug APK with -r first" >&2; exit 1; }
"$ADB" shell am force-stop "$PKG"
sleep 1
# A successful listing distinguishes missing companions from a transport failure.
FILES=$("$ADB" shell "run-as $PKG ls databases")
mkdir -p "$OUT_DIR"
CAPTURE=$(mktemp -d "$OUT_DIR/ontime-$(date +%Y%m%d-%H%M%S)-XXXXXX")
OUT="$CAPTURE/ontime.db"
trap 'echo "FAIL: incomplete backup retained at $CAPTURE; do not restore it" >&2' ERR
pull_file() {
    local remote="$1" local_path="$2" expected actual remote_name
    expected=$("$ADB" exec-out run-as "$PKG" wc -c "$remote")
    read -r expected remote_name <<< "$expected"
    expected=$(printf '%s' "$expected" | tr -d '\r\n')
    [[ "$expected" =~ ^[0-9]+$ ]] || return 1
    "$ADB" exec-out run-as "$PKG" cat "$remote" > "$local_path"
    actual=$(wc -c < "$local_path" | tr -d ' ')
    [ "$actual" = "$expected" ] || { echo "FAIL: truncated transfer of $remote" >&2; return 1; }
}
pull_file "$DB" "$OUT"
for suffix in -wal -shm -journal; do
    if printf '%s\n' "$FILES" | tr -d '\r' | grep -Fxq "ontime.db$suffix"; then
        pull_file "$DB$suffix" "$OUT$suffix"
    fi
done
python3 "$TOOLS_DIR/db-snapshot.py" "$OUT" "$CAPTURE/validated.db"
shasum -a 256 "$OUT" > "$CAPTURE/SHA256.txt"
for suffix in -wal -shm -journal; do
    if [ -f "$OUT$suffix" ]; then shasum -a 256 "$OUT$suffix" >> "$CAPTURE/SHA256.txt"; fi
done
trap - ERR
echo "OK: $OUT" >&2
# stdout is the source path for the restore tool's automatic safety backup.
printf '%s\n' "$OUT"
echo "App remains stopped; reinstall release with -r and verify alarms after maintenance." >&2
