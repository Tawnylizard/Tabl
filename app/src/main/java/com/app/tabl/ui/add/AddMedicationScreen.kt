package com.app.tabl.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tabl.ui.theme.MedicationColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    medicationId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(medicationId) {
        medicationId?.let { viewModel.loadForEdit(it) }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onNavigateBack()
    }

    val title = if (medicationId != null) "Редактировать" else "Новое лекарство"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Название *") },
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.dose,
                onValueChange = viewModel::onDoseChange,
                label = { Text("Доза (необязательно)") },
                placeholder = { Text("1 таблетка, 500 мг") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Цвет", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedicationColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (state.colorIndex == index) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { viewModel.onColorChange(index) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.stockCount,
                    onValueChange = viewModel::onStockCountChange,
                    label = { Text("Запас (шт.)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.stockThreshold,
                    onValueChange = viewModel::onStockThresholdChange,
                    label = { Text("Порог уведомления") },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()
            Text("Расписание", style = MaterialTheme.typography.titleMedium)

            Text("Время приёма", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.timeHour.toString().padStart(2, '0'),
                    onValueChange = { v -> v.toIntOrNull()?.coerceIn(0, 23)?.let { viewModel.onTimeChange(it, state.timeMinute) } },
                    label = { Text("Час") },
                    modifier = Modifier.width(80.dp)
                )
                Text(":")
                OutlinedTextField(
                    value = state.timeMinute.toString().padStart(2, '0'),
                    onValueChange = { v -> v.toIntOrNull()?.coerceIn(0, 59)?.let { viewModel.onTimeChange(state.timeHour, it) } },
                    label = { Text("Мин") },
                    modifier = Modifier.width(80.dp)
                )
            }

            Text("Дни недели (пусто = каждый день)", style = MaterialTheme.typography.titleSmall)
            val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                dayLabels.forEachIndexed { i, label ->
                    val day = i + 1
                    FilterChip(
                        selected = day in state.daysOfWeek,
                        onClick = { viewModel.onDayToggle(day) },
                        label = { Text(label) }
                    )
                }
            }

            Button(
                onClick = { viewModel.save(medicationId) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(24.dp))
                else Text("Сохранить")
            }
        }
    }
}
