package com.app.tabl.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {
    val snoozeMinutes = settings.snoozeMinutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)
    val maxSnoozeCount = settings.maxSnoozeCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    fun setSnooze(v: Int) { viewModelScope.launch { settings.setSnoozeMinutes(v) } }
    fun setMaxSnooze(v: Int) { viewModelScope.launch { settings.setMaxSnoozeCount(v) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val snoozeMinutes by viewModel.snoozeMinutes.collectAsState()
    val maxSnooze by viewModel.maxSnoozeCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Уведомления", style = MaterialTheme.typography.titleMedium)

            Text("Длительность снуза: $snoozeMinutes мин", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 15, 30).forEach { min ->
                    FilterChip(
                        selected = snoozeMinutes == min,
                        onClick = { viewModel.setSnooze(min) },
                        label = { Text("$min мин") }
                    )
                }
            }

            HorizontalDivider()

            Text("Макс. повторов уведомления: $maxSnooze", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { count ->
                    FilterChip(
                        selected = maxSnooze == count,
                        onClick = { viewModel.setMaxSnooze(count) },
                        label = { Text("$count") }
                    )
                }
            }

            HorizontalDivider()

            Text(
                "Версия приложения: 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
