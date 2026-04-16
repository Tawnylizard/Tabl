package com.app.tabl.domain.scheduler

import android.content.Context
import androidx.work.*
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.worker.MedicationReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MedicationRepository
) {
    private val workManager = WorkManager.getInstance(context)

    suspend fun scheduleNext(medicationId: Long, scheduleId: Long) {
        val medication = repository.getMedicationById(medicationId) ?: return
        val schedule = repository.getScheduleById(scheduleId) ?: return

        if (!medication.isActive) return
        schedule.endDate?.let { end ->
            if (java.time.LocalDate.now() > end) return
        }

        val nextTrigger = AlarmCalculator.calculateNextTrigger(schedule) ?: return
        val delayMillis = nextTrigger - System.currentTimeMillis()
        if (delayMillis < 0) return

        val data = workDataOf(
            MedicationReminderWorker.KEY_MEDICATION_ID to medicationId,
            MedicationReminderWorker.KEY_SCHEDULE_ID to scheduleId,
            MedicationReminderWorker.KEY_SCHEDULED_AT to nextTrigger
        )

        val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("med_$scheduleId")
            .build()

        workManager.enqueueUniqueWork(
            "reminder_$scheduleId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(scheduleId: Long) {
        workManager.cancelUniqueWork("reminder_$scheduleId")
        workManager.cancelAllWorkByTag("med_$scheduleId")
    }

    fun cancelAllForMedication(medicationId: Long) {
        workManager.cancelAllWorkByTag("medication_$medicationId")
    }

    suspend fun rescheduleAll() {
        val schedules = repository.getAllActiveSchedules()
        schedules.forEach { schedule ->
            scheduleNext(schedule.medicationId, schedule.id)
        }
    }

    fun scheduleSnooze(medicationId: Long, scheduleId: Long, logId: Long, scheduledAt: Long, delayMinutes: Int) {
        val data = workDataOf(
            MedicationReminderWorker.KEY_MEDICATION_ID to medicationId,
            MedicationReminderWorker.KEY_SCHEDULE_ID to scheduleId,
            MedicationReminderWorker.KEY_SCHEDULED_AT to scheduledAt,
            MedicationReminderWorker.KEY_IS_SNOOZE to true
        )

        val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(data)
            .addTag("snooze_$logId")
            .build()

        workManager.enqueueUniqueWork("snooze_$logId", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelSnooze(logId: Long) {
        workManager.cancelUniqueWork("snooze_$logId")
    }

    fun scheduleRepeatCheck(logId: Long, medicationId: Long, scheduleId: Long, scheduledAt: Long, repeatCount: Int) {
        val data = workDataOf(
            MedicationReminderWorker.KEY_MEDICATION_ID to medicationId,
            MedicationReminderWorker.KEY_SCHEDULE_ID to scheduleId,
            MedicationReminderWorker.KEY_SCHEDULED_AT to scheduledAt,
            MedicationReminderWorker.KEY_REPEAT_COUNT to repeatCount
        )

        val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(30, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag("repeat_$logId")
            .build()

        workManager.enqueueUniqueWork("repeat_$logId", ExistingWorkPolicy.REPLACE, request)
    }
}
