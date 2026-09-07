#!/usr/bin/env python3
"""Offline regression tests. A fake adb operates only inside TemporaryDirectory."""
import os
import shutil
import sqlite3
import subprocess
import tempfile
import unittest
from pathlib import Path

TOOLS = Path(__file__).resolve().parent
FAKE_ADB = r'''#!/usr/bin/env python3
import os, pathlib, sys
root = pathlib.Path(os.environ['FAKE_DEVICE'])
a = sys.argv[1:]
mode = os.environ.get('FAKE_FAILURE', '')
with (root / 'calls').open('a') as log:
    log.write(repr(a) + '\n')
if a == ['get-state']:
    print('device')
elif a[:3] == ['shell', 'dumpsys', 'package']:
    print('pkgFlags=[ DEBUGGABLE ]' if mode != 'release' else 'pkgFlags=[ HAS_CODE ]')
elif a[:3] == ['shell', 'am', 'force-stop']:
    pass
elif a == ['shell', 'run-as dev.evesis.ontime ls databases']:
    if mode == 'list': sys.exit(1)
    print('\n'.join(p.name for p in (root / 'databases').iterdir()))
elif a[:3] == ['exec-out', 'run-as', 'dev.evesis.ontime']:
    name = a[-1]
    if a[3:5] == ['wc', '-c']:
        print('   ' + str((root / name).stat().st_size) + ' ' + name)
        sys.exit(0)
    if mode == 'wal' and name.endswith('-wal'): sys.exit(1)
    data = (root / name).read_bytes()
    if mode == 'empty' and name.endswith('.db'): data = b''
    if mode == 'short-wal' and name.endswith('-wal'): data = b''
    if mode == 'readback' and (root / 'written').exists() and name.endswith('.db'):
        data = data[:-1] + bytes([data[-1] ^ 1])
    sys.stdout.buffer.write(data)
elif len(a) == 2 and a[0] == 'shell' and "sh -c 'cat > " in a[1]:
    name = a[1].split('cat > ')[1].rstrip("'")
    if mode == 'write': sys.exit(1)
    data = sys.stdin.buffer.read()
    if not (mode == 'stale' and name.endswith('-wal')):
        (root / name).write_bytes(data)
    (root / 'written').touch()
else:
    sys.exit('unexpected adb arguments: ' + repr(a))
'''


class DeviceDbTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix='ontime-db-test-')
        self.root = Path(self.tmp.name)
        self.device = self.root / 'device'
        (self.device / 'databases').mkdir(parents=True)
        self.adb = self.root / 'adb'
        self.adb.write_text(FAKE_ADB)
        self.adb.chmod(0o755)
        self.env = dict(os.environ, ADB=str(self.adb), FAKE_DEVICE=str(self.device),
                        ONTIME_BACKUP_DIR=str(self.root / 'safety'))
        self.source = self.root / 'source.db'
        self.make_db(self.source, 'source')
        self.make_db(self.device / 'databases/ontime.db', 'original')

    def tearDown(self):
        self.tmp.cleanup()

    def make_db(self, path, value):
        with sqlite3.connect(path) as c:
            c.executescript('PRAGMA user_version=4; CREATE TABLE reminders(id INTEGER PRIMARY KEY, title TEXT);'
                            'CREATE TABLE fire_log(id INTEGER PRIMARY KEY, status TEXT);')
            c.execute('INSERT INTO reminders VALUES(1, ?)', (value,))
            c.execute("INSERT INTO fire_log VALUES(1, 'slide-ack')")

    def with_wal(self):
        live = self.root / 'live.db'
        self.make_db(live, 'checkpointed')
        c = sqlite3.connect(live)
        c.execute('PRAGMA journal_mode=WAL')
        c.execute('PRAGMA wal_autocheckpoint=0')
        c.execute("UPDATE reminders SET title='wal-only'")
        c.execute("INSERT INTO fire_log VALUES(2, 'fired')")
        c.commit()
        for suffix in ('', '-wal', '-shm'):
            shutil.copyfile(str(live) + suffix, str(self.source) + suffix)
        c.close()

    def run_tool(self, name, argument, success=True, failure=''):
        result = subprocess.run([str(TOOLS / name), str(argument)],
                                env=dict(self.env, FAKE_FAILURE=failure),
                                capture_output=True, text=True, timeout=30)
        self.assertEqual(result.returncode == 0, success, result.stdout + result.stderr)
        return result

    def test_wal_snapshot_and_stale_device_journals(self):
        self.with_wal()
        before = {p.name: p.read_bytes() for p in self.root.glob('source.db*')}
        # Valid device WAL with pending changes must be backed up before replacement.
        for suffix in ('', '-wal', '-shm'):
            shutil.copyfile(str(self.source) + suffix, str(self.device / 'databases/ontime.db') + suffix)
        self.run_tool('restore-device-db.sh', self.source)
        for name, contents in before.items():
            self.assertEqual((self.root / name).read_bytes(), contents)
        with sqlite3.connect(self.device / 'databases/ontime.db') as c:
            self.assertEqual(c.execute('SELECT title FROM reminders').fetchone(), ('wal-only',))
            self.assertEqual(c.execute('SELECT COUNT(*) FROM fire_log').fetchone(), (2,))
        backup = next((self.root / 'safety').glob('*/ontime.db'))
        self.assertGreater(Path(str(backup) + '-wal').stat().st_size, 0)
        for suffix in ('-wal', '-shm', '-journal'):
            self.assertEqual(Path(str(self.device / 'databases/ontime.db') + suffix).stat().st_size, 0)

    def test_no_wal_and_same_row_count_different_content(self):
        self.run_tool('restore-device-db.sh', self.source)
        with sqlite3.connect(self.device / 'databases/ontime.db') as c:
            self.assertEqual(c.execute('SELECT title FROM reminders').fetchone(), ('source',))
        backup = next((self.root / 'safety').glob('*/validated.db'))
        with sqlite3.connect(backup) as c:
            self.assertEqual(c.execute('SELECT title FROM reminders').fetchone(), ('original',))

    def test_invalid_source_never_contacts_device(self):
        for data in (b'', b'not sqlite' * 1000, b'SQLite format 3\0' + b'\0' * 8192):
            self.source.write_bytes(data)
            self.run_tool('restore-device-db.sh', self.source, success=False)
            self.assertFalse((self.device / 'calls').exists())

    def test_legacy_wal_name_is_rejected(self):
        (self.root / 'source-wal').write_bytes(b'legacy')
        self.run_tool('restore-device-db.sh', self.source, success=False)
        self.assertFalse((self.device / 'calls').exists())

    def test_failed_backup_prevents_restore(self):
        (self.device / 'databases/ontime.db-wal').touch()
        for failure in ('release', 'list', 'wal', 'empty'):
            self.run_tool('restore-device-db.sh', self.source, success=False, failure=failure)
            self.assertFalse((self.device / 'written').exists())

    def test_silent_short_wal_prevents_restore(self):
        self.with_wal()
        for suffix in ('', '-wal', '-shm'):
            shutil.copyfile(str(self.source) + suffix, str(self.device / 'databases/ontime.db') + suffix)
        self.run_tool('restore-device-db.sh', self.source, success=False, failure='short-wal')
        self.assertFalse((self.device / 'written').exists())

    def test_write_failure_preserves_safety_backup(self):
        self.run_tool('restore-device-db.sh', self.source, success=False, failure='write')
        self.assertTrue(list((self.root / 'safety').glob('*/validated.db')))
        self.assertFalse((self.device / 'written').exists())

    def test_readback_mismatch_is_failure(self):
        self.run_tool('restore-device-db.sh', self.source, success=False, failure='readback')
        self.assertTrue(list((self.root / 'safety').glob('*/validated.db')))

    def test_stale_wal_is_failure(self):
        self.with_wal()
        for suffix in ('', '-wal', '-shm'):
            shutil.copyfile(str(self.source) + suffix, str(self.device / 'databases/ontime.db') + suffix)
        self.run_tool('restore-device-db.sh', self.source, success=False, failure='stale')

    def test_backup_preserves_companions(self):
        self.with_wal()
        for suffix in ('', '-wal', '-shm'):
            shutil.copyfile(str(self.source) + suffix, str(self.device / 'databases/ontime.db') + suffix)
        result = self.run_tool('backup-device-db.sh', self.root / 'captures')
        backup = Path(result.stdout.strip())
        for suffix in ('', '-wal', '-shm'):
            self.assertEqual(Path(str(backup) + suffix).read_bytes(),
                             Path(str(self.source) + suffix).read_bytes())


if __name__ == '__main__':
    unittest.main(verbosity=2)
