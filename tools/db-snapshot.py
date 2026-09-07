#!/usr/bin/env python3
"""Create a checked standalone snapshot without modifying the source DB/WAL."""
from contextlib import closing
import shutil
import sqlite3
import sys
import tempfile
from pathlib import Path


def snapshot(source, destination):
    if destination.exists():
        raise ValueError("snapshot destination already exists")
    with source.open("rb") as stream:
        if source.stat().st_size < 4096 or stream.read(16) != b"SQLite format 3\0":
            raise ValueError("not a non-empty SQLite database")
    # Old script output used these wrong names. Never silently discard that WAL.
    for suffix in ("-wal", "-shm"):
        legacy = source.with_name(source.stem + suffix)
        if legacy.exists():
            raise ValueError("ambiguous legacy companion: " + str(legacy))
    with tempfile.TemporaryDirectory(prefix="ontime-snapshot-") as tmp:
        copied = Path(tmp) / "source.db"
        shutil.copyfile(source, copied)
        for suffix in ("-wal", "-shm", "-journal"):
            companion = Path(str(source) + suffix)
            if companion.exists():
                shutil.copyfile(companion, Path(str(copied) + suffix))
        with closing(sqlite3.connect(copied)) as connection:
            if connection.execute("PRAGMA integrity_check").fetchall() != [("ok",)]:
                raise ValueError("source integrity_check failed")
            for table in ("reminders", "fire_log"):
                connection.execute("SELECT COUNT(*) FROM " + table).fetchone()
            if connection.execute("PRAGMA user_version").fetchone()[0] not in (3, 4):
                raise ValueError("unsupported OnTime schema (expected 3 or 4)")
            with closing(sqlite3.connect(destination)) as output:
                connection.backup(output)
                if output.execute("PRAGMA journal_mode=DELETE").fetchone() != ("delete",):
                    raise ValueError("snapshot did not leave WAL mode")
                if output.execute("PRAGMA integrity_check").fetchall() != [("ok",)]:
                    raise ValueError("snapshot integrity_check failed")


if __name__ == "__main__":
    try:
        snapshot(Path(sys.argv[1]), Path(sys.argv[2]))
    except (OSError, sqlite3.Error, ValueError) as error:
        sys.exit("FAIL: " + str(error))
