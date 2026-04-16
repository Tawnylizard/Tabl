package com.app.tabl.data.local.dao

import androidx.room.*
import com.app.tabl.data.local.entity.MedicationLogEntity
import com.app.tabl.domain.model.LogStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {

    @Query("SELECT * FROM medication_logs WHERE medication_id = :medicationId AND scheduled_at BETWEEN :from AND :to ORDER BY scheduled_at DESC")
    fun getLogsForMedication(medicationId: Long, from: Long, to: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE scheduled_at BETWEEN :from AND :to ORDER BY scheduled_at DESC")
    fun getAllLogs(from: Long, to: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE id = :id")
    suspend fun getById(id: Long): MedicationLogEntity?

    @Query("SELECT * FROM medication_logs WHERE schedule_id = :scheduleId AND scheduled_at = :scheduledAt LIMIT 1")
    suspend fun getByScheduleAndTime(scheduleId: Long, scheduledAt: Long): MedicationLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MedicationLogEntity): Long

    @Update
    suspend fun update(log: MedicationLogEntity)

    @Query("UPDATE medication_logs SET status = :status, action_at = :actionAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: LogStatus, actionAt: Long)

    @Query("UPDATE medication_logs SET status = :status, action_at = :actionAt, snooze_count = :snoozeCount WHERE id = :id")
    suspend fun updateStatusAndSnoozeCount(id: Long, status: LogStatus, actionAt: Long, snoozeCount: Int)

    @Query("SELECT COUNT(*) FROM medication_logs WHERE medication_id = :medicationId AND status = :status AND scheduled_at BETWEEN :from AND :to")
    suspend fun countByStatus(medicationId: Long, status: LogStatus, from: Long, to: Long): Int
}
