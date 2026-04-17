package com.app.tabl.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tabl.ui.home.components.MedicationCard
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUpcoming: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val expiringSchedules by viewModel.expiringSchedules.collectAsState()

    // Track which expiring schedules have been dismissed this session
    var dismissedIds by remember { mutableStateOf(setOf<Long>()) }
    val pendingExpiry = remember(expiringSchedules, dismissedIds) {
        expiringSchedules.filterNot { it.schedule.id in dismissedIds }
    }
    val currentExpiry = pendingExpiry.firstOrNull()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* user chose — show banner if denied */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Expiry dialog for the first pending expiring schedule
    if (currentExpiry != null) {
        val endDateStr = remember(currentExpiry) {
            currentExpiry.schedule.endDate
                ?.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))
                ?: ""
        }
        AlertDialog(
            onDismissRequest = { dismissedIds = dismissedIds + currentExpiry.schedule.id },
            title = { Text("Расписание заканчивается") },
            text = {
                Text(
                    "Приём «${currentExpiry.medication.name}» заканчивается $endDateStr. " +
                    "Продлить расписание на 30 дней?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.extendSchedule(currentExpiry, days = 30)
                    dismissedIds = dismissedIds + currentExpiry.schedule.id
                }) {
                    Text("Продлить")
                }
            },
            dismissButton = {
                TextButton(onClick = { dismissedIds = dismissedIds + currentExpiry.schedule.id }) {
                    Text("Пропустить")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabl") },
                actions = {
                    IconButton(onClick = onOpenUpcoming) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Расписание")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "История")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Добавить лекарство")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is HomeUiState.Empty -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Нет лекарств", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Нажмите + чтобы добавить", style = MaterialTheme.typography.bodyMedium)
                }
            }

            is HomeUiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.medications, key = { it.id }) { med ->
                    MedicationCard(
                        medication = med,
                        onEdit = { onEditMedication(med.id) },
                        onDelete = { viewModel.delete(med) },
                        onToggleActive = { viewModel.toggleActive(med) }
                    )
                }
            }
        }
    }
}
