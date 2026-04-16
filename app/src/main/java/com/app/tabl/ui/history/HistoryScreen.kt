package com.app.tabl.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tabl.domain.model.LogStatus
import com.app.tabl.domain.model.MedicationLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val compliance by viewModel.compliancePercent.collectAsState()
    val selectedMedId by viewModel.selectedMedicationId.collectAsState()
    val periodDays by viewModel.periodDays.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История приёмов") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Period selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30).forEach { days ->
                    FilterChip(
                        selected = periodDays == days,
                        onClick = { viewModel.setPeriod(days) },
                        label = { Text("$days дней") }
                    )
                }
            }

            // Medication filter
            if (medications.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val selectedName = medications.find { it.id == selectedMedId }?.name ?: "Все лекарства"
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Лекарство") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Все лекарства") }, onClick = {
                            viewModel.selectMedication(null); expanded = false
                        })
                        medications.forEach { med ->
                            DropdownMenuItem(text = { Text(med.name) }, onClick = {
                                viewModel.selectMedication(med.id); expanded = false
                            })
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Compliance badge
            if (selectedMedId != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Соблюдение режима:", modifier = Modifier.weight(1f))
                        Text(
                            "$compliance%",
                            style = MaterialTheme.typography.titleLarge,
                            color = when {
                                compliance >= 80 -> MaterialTheme.colorScheme.primary
                                compliance >= 50 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Log list
            if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет записей за выбранный период")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(logs, key = { it.id }) { log ->
                        LogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: MedicationLog) {
    val fmt = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }
    val icon = when (log.status) {
        LogStatus.TAKEN -> "✅"
        LogStatus.MISSED -> "❌"
        LogStatus.SKIPPED -> "⏭️"
        LogStatus.SNOOZED -> "⏰"
    }
    val statusText = when (log.status) {
        LogStatus.TAKEN -> "Принято"
        LogStatus.MISSED -> "Пропущено"
        LogStatus.SKIPPED -> "Пропущено вручную"
        LogStatus.SNOOZED -> "Отложено"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, modifier = Modifier.width(32.dp))
        Column(Modifier.weight(1f)) {
            Text(fmt.format(Date(log.scheduledAt)), style = MaterialTheme.typography.bodySmall)
            Text(statusText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
