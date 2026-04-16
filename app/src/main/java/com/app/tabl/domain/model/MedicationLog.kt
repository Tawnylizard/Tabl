package com.app.tabl.domain.model

enum class LogStatus { TAKEN, MISSED, SNOOZED, SKIPPED }

data class MedicationLog(
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledAt: Long,
    val actionAt: Long? = null,
    val status: LogStatus = LogStatus.MISSED,
    val snoozeCount: Int = 0
)
