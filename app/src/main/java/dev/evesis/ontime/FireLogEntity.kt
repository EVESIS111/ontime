package dev.evesis.ontime

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 触发日志(结构与历史 schema 一致;上限 1000 条,插入时裁剪) */
@Entity(
    tableName = "fire_log",
    indices = [Index(value = ["actual_at"], name = "idx_fire_actual", orders = [Index.Order.DESC])],
)
data class FireLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "reminder_id") val reminderId: Long,
    val title: String,
    @ColumnInfo(name = "planned_at") val plannedAt: Long,
    @ColumnInfo(name = "actual_at") val actualAt: Long,
    @ColumnInfo(name = "latency_ms") val latencyMs: Long,
    val audio: String,
    val note: String,
)
