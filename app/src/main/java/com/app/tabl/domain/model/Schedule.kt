package com.app.tabl.domain.model

import java.time.LocalDate

data class Schedule(
    val id: Long = 0,
    val medicationId: Long,
    val timeHour: Int,
    val timeMinute: Int,
    val daysOfWeek: Set<Int> = emptySet(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
