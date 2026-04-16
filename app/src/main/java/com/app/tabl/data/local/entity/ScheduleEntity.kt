package com.app.tabl.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.tabl.domain.model.Schedule
import java.time.LocalDate

@Entity(
    tableName = "schedules",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class,
        parentColumns = ["id"],
        childColumns = ["medication_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id", index = true) val medicationId: Long,
    @ColumnInfo(name = "time_hour") val timeHour: Int,
    @ColumnInfo(name = "time_minute") val timeMinute: Int,
    @ColumnInfo(name = "days_of_week") val daysOfWeek: Set<Int> = emptySet(),
    @ColumnInfo(name = "start_date") val startDate: LocalDate? = null,
    @ColumnInfo(name = "end_date") val endDate: LocalDate? = null
) {
    fun toDomain() = Schedule(id, medicationId, timeHour, timeMinute, daysOfWeek, startDate, endDate)
}

fun Schedule.toEntity() = ScheduleEntity(id, medicationId, timeHour, timeMinute, daysOfWeek, startDate, endDate)
