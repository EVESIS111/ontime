package dev.evesis.ontime

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room 数据库(PHASE8,KMP-ready 设计:Entity/DAO 为纯声明,无 Android API,
 * 未来可整体进入 commonMain;Android 特有的构建(builder/databaseBuilder)只在此 companion)。
 * version=3 与历史 SQLiteOpenHelper 对齐:schema 逐列一致 → 旧库直接打开,无 Migration。
 */
@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY id ASC")
    fun list(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun find(id: Long): Reminder?

    @Insert
    fun insert(r: Reminder): Long

    @Update
    fun update(r: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    fun delete(id: Long)

    @Query("UPDATE reminders SET next_fire_at = :t WHERE id = :id")
    fun setNextFire(id: Long, t: Long)

    @Query("UPDATE reminders SET last_fired_at = :t WHERE id = :id")
    fun setLastFired(id: Long, t: Long)

    @Query("UPDATE reminders SET enabled = :on WHERE id = :id")
    fun setEnabled(id: Long, on: Boolean)
}

@Dao
interface FireLogDao {
    @Insert
    fun insert(e: FireLogEntity): Long

    @Query("SELECT * FROM fire_log WHERE id = :id")
    fun find(id: Long): FireLogEntity?

    @Query("UPDATE fire_log SET note = note || ' | ' || :note WHERE id = :id")
    fun appendNote(id: Long, note: String)

    @Query("DELETE FROM fire_log WHERE id NOT IN (SELECT id FROM fire_log ORDER BY actual_at DESC LIMIT 1000)")
    fun trimTo1000()
}

class Converters {
    @TypeConverter
    fun repeatTypeToString(rt: ScheduleEngine.RepeatType): String = rt.name

    @TypeConverter
    fun stringToRepeatType(s: String): ScheduleEngine.RepeatType = ScheduleEngine.RepeatType.valueOf(s)
}

@Database(
    entities = [Reminder::class, FireLogEntity::class],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class OnTimeDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun fireLogDao(): FireLogDao
}

/**
 * v3(手写 SQLiteOpenHelper)→ v4(Room):唯一实质差异是 PK 列显式 NOT NULL
 * (Room 校验要求 pragma notnull=1;旧建表语句 `INTEGER PRIMARY KEY AUTOINCREMENT`
 * 不带显式 NOT NULL,pragma 报 0 → "Pre-packaged database has an invalid schema")。
 * 重建两张表并原样拷贝全部数据;DDL 与 app/schemas 4.json 的 createSql 逐字一致。
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `_tmp_reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `audio_uri` TEXT, `voice_id` TEXT NOT NULL DEFAULT '', `sound_id` TEXT NOT NULL DEFAULT 'coin', `repeat_type` TEXT NOT NULL, `time_of_day` INTEGER NOT NULL DEFAULT 0, `week_mask` INTEGER NOT NULL DEFAULT 0, `interval_minutes` INTEGER NOT NULL DEFAULT 60, `at_millis` INTEGER NOT NULL DEFAULT 0, `snooze_minutes` INTEGER NOT NULL DEFAULT 10, `enabled` INTEGER NOT NULL DEFAULT 1, `next_fire_at` INTEGER NOT NULL DEFAULT 0, `last_fired_at` INTEGER NOT NULL DEFAULT 0, `window_start` INTEGER NOT NULL DEFAULT -1, `window_end` INTEGER NOT NULL DEFAULT -1)")
        db.execSQL("INSERT INTO `_tmp_reminders` (`id`,`title`,`message`,`audio_uri`,`voice_id`,`sound_id`,`repeat_type`,`time_of_day`,`week_mask`,`interval_minutes`,`at_millis`,`snooze_minutes`,`enabled`,`next_fire_at`,`last_fired_at`,`window_start`,`window_end`) SELECT `id`,`title`,`message`,`audio_uri`,`voice_id`,`sound_id`,`repeat_type`,`time_of_day`,`week_mask`,`interval_minutes`,`at_millis`,`snooze_minutes`,`enabled`,`next_fire_at`,`last_fired_at`,`window_start`,`window_end` FROM `reminders`")
        db.execSQL("DROP TABLE `reminders`")
        db.execSQL("ALTER TABLE `_tmp_reminders` RENAME TO `reminders`")

        db.execSQL("CREATE TABLE IF NOT EXISTS `_tmp_fire_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `reminder_id` INTEGER NOT NULL, `title` TEXT NOT NULL, `planned_at` INTEGER NOT NULL, `actual_at` INTEGER NOT NULL, `latency_ms` INTEGER NOT NULL, `audio` TEXT NOT NULL, `note` TEXT NOT NULL)")
        db.execSQL("INSERT INTO `_tmp_fire_log` (`id`,`reminder_id`,`title`,`planned_at`,`actual_at`,`latency_ms`,`audio`,`note`) SELECT `id`,`reminder_id`,`title`,`planned_at`,`actual_at`,`latency_ms`,`audio`,`note` FROM `fire_log`")
        db.execSQL("DROP TABLE `fire_log`")
        db.execSQL("ALTER TABLE `_tmp_fire_log` RENAME TO `fire_log`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_fire_actual` ON `fire_log` (`actual_at` DESC)")
    }
}
