package com.app.tabl.data.repository

import com.app.tabl.data.local.dao.MedicationLogDao
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LogRepositorySnoozeTest {

    private val dao: MedicationLogDao = mockk()
    private val repo = LogRepository(dao)

    private fun log(snoozeCount: Int = 0) = MedicationLog(
        id = 1L,
        medicationId = 1L,
        scheduleId = 1L,
        scheduledAt = 1000L,
        status = LogStatus.MISSED,
        snoozeCount = snoozeCount
    )

    @Test
    fun `updateLogSnoozed writes SNOOZED status and incremented count`() = runTest {
        coEvery { dao.updateStatusAndSnoozeCount(1L, LogStatus.SNOOZED, any(), 1) } just Runs

        repo.updateLogSnoozed(1L, actionAt = System.currentTimeMillis(), newSnoozeCount = 1)

        coVerify { dao.updateStatusAndSnoozeCount(1L, LogStatus.SNOOZED, any(), 1) }
    }

    @Test
    fun `getLogById returns domain model with correct snoozeCount`() = runTest {
        val entity = com.app.tabl.data.local.entity.MedicationLogEntity(
            id = 1L, medicationId = 1L, scheduleId = 1L,
            scheduledAt = 1000L, status = LogStatus.SNOOZED, snoozeCount = 2
        )
        coEvery { dao.getById(1L) } returns entity

        val result = repo.getLogById(1L)
        assertEquals(2, result?.snoozeCount)
        assertEquals(LogStatus.SNOOZED, result?.status)
    }
}
