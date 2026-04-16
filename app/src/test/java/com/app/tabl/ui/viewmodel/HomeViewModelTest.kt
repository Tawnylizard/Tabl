package com.app.tabl.ui.viewmodel

import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.scheduler.NotificationScheduler
import com.app.tabl.ui.home.HomeUiState
import com.app.tabl.ui.home.HomeViewModel
import io.mockk.coEvery
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val medicationRepo: MedicationRepository = mockk()
    private val scheduler: NotificationScheduler = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        every { medicationRepo.getActiveMedications() } returns flowOf(emptyList())
        val vm = HomeViewModel(medicationRepo, scheduler)
        assertEquals(HomeUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `empty list transitions to Empty state`() = runTest {
        every { medicationRepo.getActiveMedications() } returns flowOf(emptyList())
        val vm = HomeViewModel(medicationRepo, scheduler)
        advanceUntilIdle()
        assertEquals(HomeUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `non-empty list transitions to Success state`() = runTest {
        val meds = listOf(
            Medication(id = 1L, name = "Аспирин", dose = "100мг")
        )
        every { medicationRepo.getActiveMedications() } returns flowOf(meds)
        val vm = HomeViewModel(medicationRepo, scheduler)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(1, (state as HomeUiState.Success).medications.size)
        assertEquals("Аспирин", state.medications[0].name)
    }

    @Test
    fun `delete calls cancel for each schedule then deletes medication`() = runTest {
        val med = Medication(id = 1L, name = "Метформин")
        every { medicationRepo.getActiveMedications() } returns flowOf(listOf(med))
        coEvery { medicationRepo.getSchedulesOnce(1L) } returns emptyList()
        coEvery { medicationRepo.deleteMedication(med) } returns Unit

        val vm = HomeViewModel(medicationRepo, scheduler)
        advanceUntilIdle()
        vm.delete(med)
        advanceUntilIdle()

        io.mockk.coVerify { medicationRepo.deleteMedication(med) }
    }
}
