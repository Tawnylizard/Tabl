package com.app.tabl.data.repository

import com.app.tabl.data.local.dao.MedicationLogDao
import com.app.tabl.domain.model.LogStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LogRepositoryTest {

    private val dao: MedicationLogDao = mockk()
    private val repo = LogRepository(dao)

    @Test
    fun `compliance is 0 when no logs`() = runTest {
        coEvery { dao.countByStatus(any(), LogStatus.TAKEN, any(), any()) } returns 0
        coEvery { dao.countByStatus(any(), LogStatus.MISSED, any(), any()) } returns 0
        coEvery { dao.countByStatus(any(), LogStatus.SKIPPED, any(), any()) } returns 0

        val result = repo.getCompliancePercent(1L, 0L, Long.MAX_VALUE)
        assertEquals(0, result)
    }

    @Test
    fun `compliance is 100 when all taken`() = runTest {
        coEvery { dao.countByStatus(any(), LogStatus.TAKEN, any(), any()) } returns 7
        coEvery { dao.countByStatus(any(), LogStatus.MISSED, any(), any()) } returns 0
        coEvery { dao.countByStatus(any(), LogStatus.SKIPPED, any(), any()) } returns 0

        val result = repo.getCompliancePercent(1L, 0L, Long.MAX_VALUE)
        assertEquals(100, result)
    }

    @Test
    fun `compliance formula excludes SNOOZED from denominator`() = runTest {
        // 5 taken, 3 missed, 2 skipped → compliance = 5/(5+3+2)*100 = 50
        coEvery { dao.countByStatus(any(), LogStatus.TAKEN, any(), any()) } returns 5
        coEvery { dao.countByStatus(any(), LogStatus.MISSED, any(), any()) } returns 3
        coEvery { dao.countByStatus(any(), LogStatus.SKIPPED, any(), any()) } returns 2

        val result = repo.getCompliancePercent(1L, 0L, Long.MAX_VALUE)
        assertEquals(50, result)
    }

    @Test
    fun `compliance rounds down`() = runTest {
        // 2 taken, 1 missed → 2/3 * 100 = 66
        coEvery { dao.countByStatus(any(), LogStatus.TAKEN, any(), any()) } returns 2
        coEvery { dao.countByStatus(any(), LogStatus.MISSED, any(), any()) } returns 1
        coEvery { dao.countByStatus(any(), LogStatus.SKIPPED, any(), any()) } returns 0

        val result = repo.getCompliancePercent(1L, 0L, Long.MAX_VALUE)
        assertEquals(66, result)
    }
}
