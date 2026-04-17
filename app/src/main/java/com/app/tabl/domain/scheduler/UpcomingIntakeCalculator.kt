package com.app.tabl.domain.scheduler

import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.model.Schedule
import com.app.tabl.domain.model.UpcomingIntake
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object UpcomingIntakeCalculator {

    fun calculate(
        medication: Medication,
        schedule: Schedule,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<UpcomingIntake> {
        val result = mutableListOf<UpcomingIntake>()
        var current = fromDate

        while (!current.isAfter(toDate)) {
            if (schedule.endDate != null && current.isAfter(schedule.endDate)) break
            if (schedule.startDate != null && current.isBefore(schedule.startDate)) {
                current = current.plusDays(1)
                continue
            }

            val dayOfWeek = current.dayOfWeek.value // 1=Mon..7=Sun
            if (schedule.daysOfWeek.isEmpty() || dayOfWeek in schedule.daysOfWeek) {
                val dt = LocalDateTime.of(current, LocalTime.of(schedule.timeHour, schedule.timeMinute))
                val ts = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                result.add(UpcomingIntake(medication, schedule, ts))
            }

            current = current.plusDays(1)
        }

        return result
    }
}
