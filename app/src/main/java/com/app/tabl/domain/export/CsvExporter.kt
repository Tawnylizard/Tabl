package com.app.tabl.domain.export

import com.app.tabl.domain.model.MedicationLog
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    fun buildCsv(logs: List<MedicationLog>, medicationNames: Map<Long, String>): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("medication,scheduled_at,action_at,status,snooze_count")
        logs.forEach { log ->
            val name = medicationNames[log.medicationId]?.replace(",", " ") ?: log.medicationId.toString()
            val scheduled = fmt.format(Date(log.scheduledAt))
            val actionAt = log.actionAt?.let { fmt.format(Date(it)) } ?: ""
            sb.appendLine("$name,$scheduled,$actionAt,${log.status.name},${log.snoozeCount}")
        }
        return sb.toString()
    }
}
