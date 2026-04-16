package com.app.tabl.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.app.tabl.R
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.scheduler.AlarmCalculator
import com.app.tabl.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class TablWidget : AppWidgetProvider() {

    @Inject lateinit var medicationRepository: MedicationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            val schedules = medicationRepository.getAllActiveSchedules()
            val now = LocalDateTime.now()

            data class NextEntry(val name: String, val triggerMs: Long)

            val next = schedules
                .mapNotNull { schedule ->
                    val triggerMs = AlarmCalculator.calculateNextTrigger(schedule, now) ?: return@mapNotNull null
                    val med = medicationRepository.getMedicationById(schedule.medicationId) ?: return@mapNotNull null
                    if (!med.isActive) return@mapNotNull null
                    NextEntry(med.name, triggerMs)
                }
                .minByOrNull { it.triggerMs }

            val openAppIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            for (id in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_tabl)

                if (next != null) {
                    val time = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(next.triggerMs),
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("HH:mm"))
                    views.setTextViewText(R.id.widget_medication_name, next.name)
                    views.setTextViewText(R.id.widget_medication_time, "Следующий приём: $time")
                } else {
                    views.setTextViewText(R.id.widget_medication_name, "Нет активных лекарств")
                    views.setTextViewText(R.id.widget_medication_time, "")
                }

                views.setOnClickPendingIntent(R.id.widget_btn_taken, openAppIntent)
                views.setOnClickPendingIntent(R.id.widget_medication_name, openAppIntent)

                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }
}
