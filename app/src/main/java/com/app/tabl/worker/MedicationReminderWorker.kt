package com.app.tabl.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.tabl.R
import com.app.tabl.data.repository.LogRepository
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog
import com.app.tabl.domain.scheduler.NotificationScheduler
import com.app.tabl.receiver.NotificationActionReceiver
import com.app.tabl.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val medicationRepository: MedicationRepository,
    private val logRepository: LogRepository,
    private val scheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MEDICATION_ID = "medicationId"
        const val KEY_SCHEDULE_ID = "scheduleId"
        const val KEY_SCHEDULED_AT = "scheduledAt"
        const val KEY_IS_SNOOZE = "isSnooze"
        const val KEY_REPEAT_COUNT = "repeatCount"

        const val CHANNEL_REMINDERS = "tabl_reminders"
        const val CHANNEL_STOCK = "tabl_stock"

        const val MAX_REPEAT_COUNT = 2
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val medicationId = inputData.getLong(KEY_MEDICATION_ID, -1L).takeIf { it >= 0 } ?: return@withContext Result.success()
        val scheduleId = inputData.getLong(KEY_SCHEDULE_ID, -1L).takeIf { it >= 0 } ?: return@withContext Result.success()
        val scheduledAt = inputData.getLong(KEY_SCHEDULED_AT, System.currentTimeMillis())
        val repeatCount = inputData.getInt(KEY_REPEAT_COUNT, 0)

        val medication = medicationRepository.getMedicationById(medicationId) ?: return@withContext Result.success()
        if (!medication.isActive) return@withContext Result.success()

        ensureNotificationChannel()

        val logId = logRepository.insertLog(
            MedicationLog(
                medicationId = medicationId,
                scheduleId = scheduleId,
                scheduledAt = scheduledAt,
                status = LogStatus.MISSED,
                snoozeCount = 0
            )
        )

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun actionIntent(action: String) = PendingIntent.getBroadcast(
            applicationContext,
            scheduleId.toInt(),
            Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                this.action = action
                putExtra(NotificationActionReceiver.EXTRA_LOG_ID, logId)
                putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
                putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val doseText = if (!medication.dose.isNullOrBlank()) "${medication.dose} — время принять" else "Время принять"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(medication.name)
            .setContentText(doseText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(0, "✅ Принял", actionIntent(NotificationActionReceiver.ACTION_TAKEN))
            .addAction(0, "⏰ Снуз", actionIntent(NotificationActionReceiver.ACTION_SNOOZE))
            .addAction(0, "✗ Пропустить", actionIntent(NotificationActionReceiver.ACTION_SKIP))
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)

        if (repeatCount < MAX_REPEAT_COUNT) {
            scheduler.scheduleRepeatCheck(logId, medicationId, scheduleId, scheduledAt, repeatCount + 1)
        }

        Result.success()
    }

    private fun ensureNotificationChannel() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_REMINDERS) == null) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Напоминания о лекарствах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о времени приёма лекарств"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
        if (manager.getNotificationChannel(CHANNEL_STOCK) == null) {
            val channel = NotificationChannel(
                CHANNEL_STOCK,
                "Пополнение запасов",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о низком запасе таблеток"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
