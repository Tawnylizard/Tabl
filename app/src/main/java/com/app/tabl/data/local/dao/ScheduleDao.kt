package com.app.tabl.data.local.dao

import androidx.room.*
import com.app.tabl.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules WHERE medication_id = :medicationId")
    fun getByMedicationId(medicationId: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE medication_id = :medicationId")
    suspend fun getByMedicationIdOnce(medicationId: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): ScheduleEntity?

    @Query("SELECT s.* FROM schedules s JOIN medications m ON s.medication_id = m.id WHERE m.is_active = 1")
    suspend fun getAllActiveSchedules(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleEntity>)

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE medication_id = :medicationId")
    suspend fun deleteByMedicationId(medicationId: Long)
}
