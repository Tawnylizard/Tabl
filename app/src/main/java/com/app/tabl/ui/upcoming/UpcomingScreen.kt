package com.app.tabl.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tabl.domain.model.UpcomingIntake
import com.app.tabl.ui.theme.MedicationColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpcomingViewModel = hiltViewModel()
) {
    val upcomingByDay by viewModel.upcomingByDay.collectAsState()
    val today = remember { LocalDate.now() }
    val tomorrow = remember { today.plusDays(1) }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMM, EEE", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание на месяц") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (upcomingByDay.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет запланированных приёмов", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                upcomingByDay.forEach { (date, intakes) ->
                    item(key = date.toString()) {
                        DayHeader(
                            label = when (date) {
                                today -> "Сегодня"
                                tomorrow -> "Завтра"
                                else -> date.format(dayFormatter).replaceFirstChar { it.uppercase() }
                            }
                        )
                    }
                    items(intakes, key = { "${it.schedule.id}_${it.scheduledAt}" }) { intake ->
                        UpcomingIntakeItem(intake)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun UpcomingIntakeItem(intake: UpcomingIntake) {
    val timeStr = remember(intake.scheduledAt) {
        val h = intake.schedule.timeHour.toString().padStart(2, '0')
        val m = intake.schedule.timeMinute.toString().padStart(2, '0')
        "$h:$m"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    MedicationColors.getOrElse(intake.medication.colorIndex) { MedicationColors[0] }
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(intake.medication.name, style = MaterialTheme.typography.bodyMedium)
            if (!intake.medication.dose.isNullOrBlank()) {
                Text(
                    intake.medication.dose,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            timeStr,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
