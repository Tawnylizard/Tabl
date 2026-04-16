package com.app.tabl.data.repository

import com.app.tabl.data.local.dao.MedicationLogDao
import com.app.tabl.data.local.entity.toEntity
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logDao: MedicationLogDao
) {

    suspend fun insertLog(log: MedicationLog): Long =
        logDao.insert(log.toEntity())

    suspend fun getLogById(id: Long): MedicationLog? =
        logDao.getById(id)?.toDomain()

    suspend fun updateLogStatus(id: Long, status: LogStatus, actionAt: Long) {
        logDao.updateStatus(id, status, actionAt)
    }

    suspend fun updateLogSnoozed(id: Long, actionAt: Long, newSnoozeCount: Int) {
        logDao.updateStatusAndSnoozeCount(id, LogStatus.SNOOZED, actionAt, newSnoozeCount)
    }

    fun getLogsForMedication(medicationId: Long, from: Long, to: Long): Flow<List<MedicationLog>> =
        logDao.getLogsForMedication(medicationId, from, to).map { list -> list.map { it.toDomain() } }

    fun getAllLogs(from: Long, to: Long): Flow<List<MedicationLog>> =
        logDao.getAllLogs(from, to).map { list -> list.map { it.toDomain() } }

    suspend fun countByStatus(medicationId: Long, status: LogStatus, from: Long, to: Long): Int =
        logDao.countByStatus(medicationId, status, from, to)

    suspend fun getCompliancePercent(medicationId: Long, from: Long, to: Long): Int {
        val taken = countByStatus(medicationId, LogStatus.TAKEN, from, to)
        val missed = countByStatus(medicationId, LogStatus.MISSED, from, to)
        val skipped = countByStatus(medicationId, LogStatus.SKIPPED, from, to)
        val total = taken + missed + skipped
        return if (total == 0) 0 else (taken * 100 / total)
    }
}
