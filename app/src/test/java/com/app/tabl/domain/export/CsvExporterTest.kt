package com.app.tabl.domain.export

import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CsvExporterTest {

    private fun log(
        id: Long = 1L,
        medId: Long = 1L,
        scheduledAt: Long = 1_700_000_000_000L,
        actionAt: Long? = null,
        status: LogStatus = LogStatus.TAKEN,
        snoozeCount: Int = 0
    ) = MedicationLog(id = id, medicationId = medId, scheduleId = 1L,
        scheduledAt = scheduledAt, actionAt = actionAt, status = status, snoozeCount = snoozeCount)

    @Test
    fun `output contains header row`() {
        val csv = CsvExporter.buildCsv(emptyList(), emptyMap())
        assertTrue(csv.startsWith("medication,scheduled_at,action_at,status,snooze_count"))
    }

    @Test
    fun `one log produces two lines including header`() {
        val csv = CsvExporter.buildCsv(listOf(log()), mapOf(1L to "Аспирин"))
        val lines = csv.trim().lines()
        assertEquals(2, lines.size)
    }

    @Test
    fun `medication name appears in data row`() {
        val csv = CsvExporter.buildCsv(listOf(log()), mapOf(1L to "Метформин"))
        assertTrue(csv.contains("Метформин"))
    }

    @Test
    fun `status name written as enum name`() {
        val csv = CsvExporter.buildCsv(listOf(log(status = LogStatus.MISSED)), mapOf(1L to "X"))
        assertTrue(csv.contains("MISSED"))
    }

    @Test
    fun `commas in medication name are replaced with spaces`() {
        val csv = CsvExporter.buildCsv(listOf(log()), mapOf(1L to "Витамин, C"))
        assertFalse(csv.lines().drop(1).first().split(",").let {
            // name field (first column) should not contain comma
            it[0].contains(",")
        })
        assertTrue(csv.contains("Витамин  C"))
    }

    @Test
    fun `actionAt null produces empty field`() {
        val csv = CsvExporter.buildCsv(listOf(log(actionAt = null)), mapOf(1L to "X"))
        val dataLine = csv.trim().lines()[1]
        val fields = dataLine.split(",")
        assertEquals("", fields[2]) // action_at column
    }

    @Test
    fun `snooze count written correctly`() {
        val csv = CsvExporter.buildCsv(listOf(log(snoozeCount = 2, status = LogStatus.SNOOZED)), mapOf(1L to "X"))
        val dataLine = csv.trim().lines()[1]
        assertTrue(dataLine.endsWith(",2"))
    }

    @Test
    fun `unknown medication id falls back to id string`() {
        val csv = CsvExporter.buildCsv(listOf(log(medId = 99L)), emptyMap())
        assertTrue(csv.contains("99"))
    }

    @Test
    fun `multiple logs produce correct line count`() {
        val logs = listOf(log(id = 1), log(id = 2), log(id = 3))
        val csv = CsvExporter.buildCsv(logs, mapOf(1L to "X"))
        assertEquals(4, csv.trim().lines().size) // header + 3 data
    }
}
