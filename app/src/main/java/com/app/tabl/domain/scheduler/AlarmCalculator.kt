package com.app.tabl.domain.scheduler

import com.app.tabl.domain.model.Schedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmCalculator {

    fun calculateNextTrigger(schedule: Schedule, now: LocalDateTime = LocalDateTime.now()): Long? {
        var candidate = LocalDateTime.of(now.toLocalDate(), LocalTime.of(schedule.timeHour, schedule.timeMinute))

        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }

        repeat(7) {
            val date: LocalDate = candidate.toLocalDate()

            schedule.startDate?.let { start ->
                if (date < start) {
                    candidate = candidate.plusDays(1)
                    return@repeat
                }
            }

            schedule.endDate?.let { end ->
                if (date > end) return null
            }

            val dayOfWeek = date.dayOfWeek.value // 1=Mon..7=Sun
            if (schedule.daysOfWeek.isEmpty() || dayOfWeek in schedule.daysOfWeek) {
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }

            candidate = candidate.plusDays(1)
        }

        return null
    }
}
