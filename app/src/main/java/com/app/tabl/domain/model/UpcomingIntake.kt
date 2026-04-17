package com.app.tabl.domain.model

data class UpcomingIntake(
    val medication: Medication,
    val schedule: Schedule,
    val scheduledAt: Long
)
