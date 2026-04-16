package com.app.tabl.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.tabl.data.repository.LogRepository
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.data.repository.SettingsRepository
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.scheduler.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKEN = "com.app.tabl.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.app.tabl.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.app.tabl.ACTION_SKIP"

        const val EXTRA_LOG_ID = "logId"
        const val EXTRA_MEDICATION_ID = "medicationId"
        const val EXTRA_SCHEDULE_ID = "scheduleId"
    }

    @Inject lateinit var logRepository: LogRepository
    @Inject lateinit var medicationRepository: MedicationRepository
    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L).takeIf { it >= 0 } ?: return
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L).takeIf { it >= 0 } ?: return
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L).takeIf { it >= 0 } ?: return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(scheduleId.toInt())

        scheduler.cancelSnooze(logId)

        val now = System.currentTimeMillis()

        scope.launch {
            when (intent.action) {
                ACTION_TAKEN -> {
                    logRepository.updateLogStatus(logId, LogStatus.TAKEN, now)
                    val medication = medicationRepository.getMedicationById(medicationId)
                    if (medication?.stockCount != null) {
                        medicationRepository.decrementStock(medicationId)
                        checkStockThreshold(context, medicationId)
                    }
                    scheduler.scheduleNext(medicationId, scheduleId)
                }

                ACTION_SNOOZE -> {
                    val log = logRepository.getLogById(logId) ?: return@launch
                    val maxSnooze = settingsRepository.maxSnoozeCount.first()

                    if (log.snoozeCount >= maxSnooze) {
                        logRepository.updateLogStatus(logId, LogStatus.SKIPPED, now)
                        scheduler.scheduleNext(medicationId, scheduleId)
                    } else {
                        logRepository.updateLogStatus(logId, LogStatus.SNOOZED, now)
                        val snoozeMinutes = settingsRepository.snoozeMinutes.first()
                        scheduler.scheduleSnooze(medicationId, scheduleId, logId, log.scheduledAt, snoozeMinutes)
                    }
                }

                ACTION_SKIP -> {
                    logRepository.updateLogStatus(logId, LogStatus.SKIPPED, now)
                    scheduler.scheduleNext(medicationId, scheduleId)
                }
            }
        }
    }

    private suspend fun checkStockThreshold(context: Context, medicationId: Long) {
        val medication = medicationRepository.getMedicationById(medicationId) ?: return
        val stock = medication.stockCount ?: return
        val threshold = medication.stockThreshold ?: return

        if (stock <= threshold) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = androidx.core.app.NotificationCompat.Builder(context, "tabl_stock")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Заканчиваются таблетки")
                .setContentText("Осталось $stock шт. ${medication.name}")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(medicationId.toInt() + 10000, notification)
        }
    }
}
