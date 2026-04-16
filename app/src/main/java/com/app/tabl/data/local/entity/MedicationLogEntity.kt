package com.app.tabl.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog

@Entity(
    tableName = "medication_logs",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class,
        parentColumns = ["id"],
        childColumns = ["medication_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["medication_id", "scheduled_at"])]
)
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Long,
    @ColumnInfo(name = "schedule_id") val scheduleId: Long,
    @ColumnInfo(name = "scheduled_at") val scheduledAt: Long,
    @ColumnInfo(name = "action_at") val actionAt: Long? = null,
    val status: LogStatus = LogStatus.MISSED,
    @ColumnInfo(name = "snooze_count") val snoozeCount: Int = 0
) {
    fun toDomain() = MedicationLog(id, medicationId, scheduleId, scheduledAt, actionAt, status, snoozeCount)
}

fun MedicationLog.toEntity() = MedicationLogEntity(id, medicationId, scheduleId, scheduledAt, actionAt, status, snoozeCount)
