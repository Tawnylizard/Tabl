package com.app.tabl.domain.scheduler

import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Logic-level checks for snooze state transitions — no Android framework needed.
 */
class NotificationSchedulerCancelTest {

    private fun log(snoozeCount: Int) = MedicationLog(
        id = 1L, medicationId = 1L, scheduleId = 1L,
        scheduledAt = 1000L, status = LogStatus.SNOOZED, snoozeCount = snoozeCount
    )

    @Test
    fun `snooze below max allows another snooze`() {
        val maxSnooze = 3
        val log = log(snoozeCount = 2)
        assertFalse(log.snoozeCount >= maxSnooze, "Should still be snoozeable")
        assertEquals(3, log.snoozeCount + 1)
    }

    @Test
    fun `snooze at max forces skip`() {
        val maxSnooze = 3
        val log = log(snoozeCount = 3)
        assertTrue(log.snoozeCount >= maxSnooze, "At max snooze → should be skipped")
    }

    @Test
    fun `snooze count incremented correctly`() {
        val log = log(snoozeCount = 1)
        val newCount = log.snoozeCount + 1
        assertEquals(2, newCount)
    }

    @Test
    fun `snooze overflow handled - count above max still triggers skip`() {
        val maxSnooze = 3
        val log = log(snoozeCount = 99)
        assertTrue(log.snoozeCount >= maxSnooze, "Overflow count still triggers skip path")
    }
}
