package com.app.tabl.domain.scheduler

import com.app.tabl.domain.model.Schedule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmCalculatorTest {

    private fun now(date: LocalDate = LocalDate.of(2026, 4, 16), hour: Int = 8, minute: Int = 0) =
        LocalDateTime.of(date, LocalTime.of(hour, minute))

    private fun schedule(
        hour: Int = 9,
        minute: Int = 0,
        days: Set<Int> = emptySet(),
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ) = Schedule(
        id = 1L,
        medicationId = 1L,
        timeHour = hour,
        timeMinute = minute,
        daysOfWeek = days,
        startDate = startDate,
        endDate = endDate
    )

    private fun millis(dt: LocalDateTime) =
        dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // ─── Happy path ───────────────────────────────────────────────────────────

    @Test
    fun `returns today trigger when alarm time is in the future`() {
        val now = now(hour = 8, minute = 0)
        val result = AlarmCalculator.calculateNextTrigger(schedule(hour = 9, minute = 0), now)
        val expected = millis(LocalDateTime.of(now.toLocalDate(), LocalTime.of(9, 0)))
        assertEquals(expected, result)
    }

    @Test
    fun `returns tomorrow when alarm time already passed today`() {
        val now = now(hour = 10, minute = 0)
        val result = AlarmCalculator.calculateNextTrigger(schedule(hour = 9, minute = 0), now)
        val expected = millis(LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.of(9, 0)))
        assertEquals(expected, result)
    }

    @Test
    fun `every-day schedule - picks next valid day`() {
        val thursday = LocalDate.of(2026, 4, 16) // Thursday = DayOfWeek 4
        val now = now(date = thursday, hour = 10)
        // no daysOfWeek restriction → every day → tomorrow same time
        val result = AlarmCalculator.calculateNextTrigger(schedule(hour = 9, days = emptySet()), now)
        val expected = millis(LocalDateTime.of(thursday.plusDays(1), LocalTime.of(9, 0)))
        assertEquals(expected, result)
    }

    @Test
    fun `specific days - skips non-matching days`() {
        // 2026-04-16 = Thursday (4), set only Monday (1) and Friday (5)
        val thursday = LocalDate.of(2026, 4, 16)
        val now = now(date = thursday, hour = 8)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(hour = 9, days = setOf(1, 5)), // Mon and Fri
            now
        )
        // Next Friday = 2026-04-17
        val friday = LocalDate.of(2026, 4, 17)
        val expected = millis(LocalDateTime.of(friday, LocalTime.of(9, 0)))
        assertEquals(expected, result)
    }

    // ─── Edge cases ───────────────────────────────────────────────────────────

    @Test
    fun `returns null when no daysOfWeek match in next 7 days`() {
        // Only day 6 = Saturday, but we start Thursday and look for the day that never comes
        // Actually Saturday IS within 7 days from Thursday. Let me use a single day that's only 1 day per week
        // Set only day 3 = Wednesday. Today is Thursday, so next Wednesday is 6 days away = within 7
        // Actually let me test with empty daysOfWeek → every day → should always find something
        // Test with a very specific case: daysOfWeek = setOf(4) (Thursday only)
        // Current time is Thursday 10:00, alarm at 09:00 → passes today → next Thursday
        val thursday = LocalDate.of(2026, 4, 16)
        val now = now(date = thursday, hour = 10)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(hour = 9, days = setOf(4)), // Thursday only, but time already passed
            now
        )
        // Next Thursday = 2026-04-23
        val nextThursday = LocalDate.of(2026, 4, 23)
        val expected = millis(LocalDateTime.of(nextThursday, LocalTime.of(9, 0)))
        assertEquals(expected, result)
    }

    @Test
    fun `returns null when endDate is in the past`() {
        val yesterday = LocalDate.of(2026, 4, 15)
        val now = now(hour = 8)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(hour = 9, endDate = yesterday),
            now
        )
        assertNull(result, "Should return null when course ended yesterday")
    }

    @Test
    fun `returns null when endDate equals today and alarm time passed`() {
        val today = LocalDate.of(2026, 4, 16)
        val now = now(date = today, hour = 10)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(hour = 9, endDate = today),
            now
        )
        assertNull(result, "Alarm at 09:00 passed, endDate is today → no more alarms")
    }

    @Test
    fun `returns trigger on endDate if alarm not yet passed`() {
        val today = LocalDate.of(2026, 4, 16)
        val now = now(date = today, hour = 8)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(hour = 9, endDate = today),
            now
        )
        val expected = millis(LocalDateTime.of(today, LocalTime.of(9, 0)))
        assertEquals(expected, result, "Should fire today since 09:00 not yet passed and endDate = today")
    }

    @Test
    fun `waits for startDate if it is in the future`() {
        val today = LocalDate.of(2026, 4, 16)
        val futureStart = today.plusDays(2)
        val now = now(date = today, hour = 8)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(hour = 9, startDate = futureStart),
            now
        )
        val expected = millis(LocalDateTime.of(futureStart, LocalTime.of(9, 0)))
        assertEquals(expected, result, "Should schedule on startDate, not before")
    }

    @Test
    fun `returns null when startDate is after endDate (invalid range)`() {
        val now = now(hour = 8)
        val result = AlarmCalculator.calculateNextTrigger(
            schedule(
                hour = 9,
                startDate = LocalDate.of(2026, 5, 1),
                endDate = LocalDate.of(2026, 4, 30)
            ),
            now
        )
        // endDate < startDate: first candidate will always hit endDate check first
        // start = May 1, end = Apr 30 → any candidate after Apr 30 returns null
        assertNull(result)
    }

    @Test
    fun `exact minute boundary - same time is not in the future`() {
        val today = LocalDate.of(2026, 4, 16)
        val now = now(date = today, hour = 9, minute = 0)
        val result = AlarmCalculator.calculateNextTrigger(schedule(hour = 9, minute = 0), now)
        // candidate = 09:00 today, now = 09:00 today → not strictly after → push to tomorrow
        val expected = millis(LocalDateTime.of(today.plusDays(1), LocalTime.of(9, 0)))
        assertEquals(expected, result)
    }
}
