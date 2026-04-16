package com.app.tabl.ui.viewmodel

import com.app.tabl.data.repository.LogRepository
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.model.MedicationLog
import com.app.tabl.ui.history.HistoryViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val logRepo: LogRepository = mockk()
    private val medicationRepo: MedicationRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { medicationRepo.getAllMedications() } returns flowOf(emptyList())
        every { logRepo.getAllLogs(any(), any()) } returns flowOf(emptyList())
        every { logRepo.getLogsForMedication(any(), any(), any()) } returns flowOf(emptyList())
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun log(status: LogStatus, id: Long = 1L) = MedicationLog(
        id = id, medicationId = 1L, scheduleId = 1L,
        scheduledAt = 1000L, status = status, snoozeCount = 0
    )

    @Test
    fun `compliance is 0 when no medication selected`() = runTest {
        val vm = HistoryViewModel(logRepo, medicationRepo)
        advanceUntilIdle()
        assertEquals(0, vm.compliancePercent.value)
    }

    @Test
    fun `compliance is 100 when all logs are TAKEN`() = runTest {
        val logs = listOf(log(LogStatus.TAKEN, 1), log(LogStatus.TAKEN, 2))
        every { logRepo.getLogsForMedication(1L, any(), any()) } returns flowOf(logs)

        val vm = HistoryViewModel(logRepo, medicationRepo)
        vm.selectMedication(1L)
        advanceUntilIdle()

        assertEquals(100, vm.compliancePercent.value)
    }

    @Test
    fun `compliance is 0 when all logs are MISSED`() = runTest {
        val logs = listOf(log(LogStatus.MISSED, 1), log(LogStatus.MISSED, 2))
        every { logRepo.getLogsForMedication(1L, any(), any()) } returns flowOf(logs)

        val vm = HistoryViewModel(logRepo, medicationRepo)
        vm.selectMedication(1L)
        advanceUntilIdle()

        assertEquals(0, vm.compliancePercent.value)
    }

    @Test
    fun `compliance is 50 with equal TAKEN and MISSED`() = runTest {
        val logs = listOf(log(LogStatus.TAKEN, 1), log(LogStatus.MISSED, 2))
        every { logRepo.getLogsForMedication(1L, any(), any()) } returns flowOf(logs)

        val vm = HistoryViewModel(logRepo, medicationRepo)
        vm.selectMedication(1L)
        advanceUntilIdle()

        assertEquals(50, vm.compliancePercent.value)
    }

    @Test
    fun `SNOOZED logs are excluded from compliance denominator`() = runTest {
        val logs = listOf(
            log(LogStatus.TAKEN, 1),
            log(LogStatus.SNOOZED, 2),
            log(LogStatus.SNOOZED, 3)
        )
        every { logRepo.getLogsForMedication(1L, any(), any()) } returns flowOf(logs)

        val vm = HistoryViewModel(logRepo, medicationRepo)
        vm.selectMedication(1L)
        advanceUntilIdle()

        // Only TAKEN(1) counts in denominator: 1/(1+0+0) = 100%
        assertEquals(100, vm.compliancePercent.value)
    }

    @Test
    fun `compliance resets to 0 when medication deselected`() = runTest {
        val logs = listOf(log(LogStatus.TAKEN, 1))
        every { logRepo.getLogsForMedication(1L, any(), any()) } returns flowOf(logs)

        val vm = HistoryViewModel(logRepo, medicationRepo)
        vm.selectMedication(1L)
        advanceUntilIdle()
        assertEquals(100, vm.compliancePercent.value)

        vm.selectMedication(null)
        advanceUntilIdle()
        assertEquals(0, vm.compliancePercent.value)
    }

    @Test
    fun `medications list is exposed from repository`() = runTest {
        val meds = listOf(Medication(id = 1L, name = "Аспирин", dose = "100мг"))
        every { medicationRepo.getAllMedications() } returns flowOf(meds)

        val vm = HistoryViewModel(logRepo, medicationRepo)
        advanceUntilIdle()

        assertEquals(1, vm.medications.value.size)
        assertEquals("Аспирин", vm.medications.value[0].name)
    }

    @Test
    fun `setPeriod updates periodDays state`() = runTest {
        val vm = HistoryViewModel(logRepo, medicationRepo)
        vm.setPeriod(30)
        advanceUntilIdle()
        assertEquals(30, vm.periodDays.value)
    }
}
