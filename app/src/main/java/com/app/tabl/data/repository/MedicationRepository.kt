package com.app.tabl.data.repository

import androidx.room.withTransaction
import com.app.tabl.data.local.AppDatabase
import com.app.tabl.data.local.dao.MedicationDao
import com.app.tabl.data.local.dao.ScheduleDao
import com.app.tabl.data.local.entity.toEntity
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val db: AppDatabase,
    private val medicationDao: MedicationDao,
    private val scheduleDao: ScheduleDao
) {

    fun getActiveMedications(): Flow<List<Medication>> =
        medicationDao.getActiveMedications().map { list -> list.map { it.toDomain() } }

    fun getAllMedications(): Flow<List<Medication>> =
        medicationDao.getAllMedications().map { list -> list.map { it.toDomain() } }

    suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getById(id)?.toDomain()

    suspend fun saveMedication(medication: Medication, schedules: List<Schedule>): Long =
        db.withTransaction {
            val medId = medicationDao.insert(medication.toEntity())
            val entities = schedules.map { it.copy(medicationId = medId).toEntity() }
            scheduleDao.insertAll(entities)
            medId
        }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.update(medication.toEntity())
    }

    suspend fun deleteMedication(medication: Medication) {
        medicationDao.delete(medication.toEntity())
    }

    suspend fun setMedicationActive(id: Long, active: Boolean) {
        medicationDao.setActive(id, active)
    }

    suspend fun decrementStock(medicationId: Long) {
        medicationDao.decrementStock(medicationId)
    }

    suspend fun updateStock(medicationId: Long, count: Int) {
        medicationDao.updateStock(medicationId, count)
    }

    fun getSchedulesForMedication(medicationId: Long): Flow<List<Schedule>> =
        scheduleDao.getByMedicationId(medicationId).map { list -> list.map { it.toDomain() } }

    suspend fun getSchedulesOnce(medicationId: Long): List<Schedule> =
        scheduleDao.getByMedicationIdOnce(medicationId).map { it.toDomain() }

    suspend fun getScheduleById(scheduleId: Long): Schedule? =
        scheduleDao.getById(scheduleId)?.toDomain()

    suspend fun saveSchedule(schedule: Schedule): Long =
        scheduleDao.insert(schedule.toEntity())

    suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.delete(schedule.toEntity())
    }

    suspend fun getAllActiveSchedules(): List<Schedule> =
        scheduleDao.getAllActiveSchedules().map { it.toDomain() }
}
