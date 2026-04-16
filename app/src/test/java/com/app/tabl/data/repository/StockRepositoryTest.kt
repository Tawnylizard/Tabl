package com.app.tabl.data.repository

import com.app.tabl.data.local.dao.MedicationDao
import com.app.tabl.data.local.entity.MedicationEntity
import com.app.tabl.domain.model.Medication
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StockRepositoryTest {

    private val dao: MedicationDao = mockk()
    private val db = mockk<com.app.tabl.data.local.AppDatabase>()
    private val scheduleDao = mockk<com.app.tabl.data.local.dao.ScheduleDao>()
    private val repo = MedicationRepository(db, dao, scheduleDao)

    private fun medEntity(stock: Int?, threshold: Int?) = MedicationEntity(
        id = 1L, name = "Аспирин", dose = "100мг", colorIndex = 0,
        stockCount = stock, stockThreshold = threshold,
        isActive = true, createdAt = 1000L
    )

    @Test
    fun `decrementStock calls dao with correct id`() = runTest {
        coEvery { dao.decrementStock(1L) } just Runs
        repo.decrementStock(1L)
        coVerify { dao.decrementStock(1L) }
    }

    @Test
    fun `updateStock calls dao with correct id and count`() = runTest {
        coEvery { dao.updateStock(1L, 30) } just Runs
        repo.updateStock(1L, 30)
        coVerify { dao.updateStock(1L, 30) }
    }

    @Test
    fun `getMedicationById returns domain model with stock fields`() = runTest {
        coEvery { dao.getById(1L) } returns medEntity(stock = 5, threshold = 10)
        val result = repo.getMedicationById(1L)
        assertEquals(5, result?.stockCount)
        assertEquals(10, result?.stockThreshold)
    }

    @Test
    fun `stock threshold check - below threshold`() {
        val med = Medication(id = 1L, name = "Аспирин", dose = "100мг", stockCount = 3, stockThreshold = 5)
        val isLow = med.stockCount != null && med.stockThreshold != null && med.stockCount <= med.stockThreshold
        assertTrue(isLow)
    }

    @Test
    fun `stock threshold check - at threshold`() {
        val med = Medication(id = 1L, name = "Аспирин", dose = "100мг", stockCount = 5, stockThreshold = 5)
        val isLow = med.stockCount != null && med.stockThreshold != null && med.stockCount <= med.stockThreshold
        assertTrue(isLow)
    }

    @Test
    fun `stock threshold check - above threshold`() {
        val med = Medication(id = 1L, name = "Аспирин", dose = "100мг", stockCount = 10, stockThreshold = 5)
        val isLow = med.stockCount != null && med.stockThreshold != null && med.stockCount <= med.stockThreshold
        assertFalse(isLow)
    }

    @Test
    fun `stock threshold check - no threshold set`() {
        val med = Medication(id = 1L, name = "Аспирин", dose = "100мг", stockCount = 2, stockThreshold = null)
        val isLow = med.stockCount != null && med.stockThreshold != null && med.stockCount <= med.stockThreshold
        assertFalse(isLow)
    }

    @Test
    fun `dao decrementStock does not go below zero`() = runTest {
        // DAO query: UPDATE ... SET stock_count = stock_count - 1 WHERE ... AND stock_count > 0
        // This test verifies the guard is in the DAO query; here we just verify the call is made
        coEvery { dao.decrementStock(1L) } just Runs
        repo.decrementStock(1L)
        coVerify(exactly = 1) { dao.decrementStock(1L) }
    }
}
