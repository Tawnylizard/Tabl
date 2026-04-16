package com.app.tabl.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    val snoozeMinutes = settings.snoozeMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SNOOZE_MINUTES)

    val maxSnoozeCount = settings.maxSnoozeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_MAX_SNOOZE_COUNT)

    val maxRepeatCount = settings.maxRepeatCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_MAX_REPEAT_COUNT)

    val theme = settings.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_THEME)

    fun setSnooze(v: Int) { viewModelScope.launch { settings.setSnoozeMinutes(v) } }
    fun setMaxSnooze(v: Int) { viewModelScope.launch { settings.setMaxSnoozeCount(v) } }
    fun setMaxRepeat(v: Int) { viewModelScope.launch { settings.setMaxRepeatCount(v) } }
    fun setTheme(t: String) { viewModelScope.launch { settings.setTheme(t) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val snoozeMinutes by viewModel.snoozeMinutes.collectAsState()
    val maxSnooze by viewModel.maxSnoozeCount.collectAsState()
    val maxRepeat by viewModel.maxRepeatCount.collectAsState()
    val theme by viewModel.theme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Уведомления", style = MaterialTheme.typography.titleMedium)

            Text("Длительность снуза", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 15, 30).forEach { min ->
                    FilterChip(
                        selected = snoozeMinutes == min,
                        onClick = { viewModel.setSnooze(min) },
                        label = { Text("$min мин") }
                    )
                }
            }

            Text("Макс. повторов снуза", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { count ->
                    FilterChip(
                        selected = maxSnooze == count,
                        onClick = { viewModel.setMaxSnooze(count) },
                        label = { Text("$count") }
                    )
                }
            }

            Text("Макс. повторов уведомления", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { count ->
                    FilterChip(
                        selected = maxRepeat == count,
                        onClick = { viewModel.setMaxRepeat(count) },
                        label = { Text("$count") }
                    )
                }
            }

            HorizontalDivider()

            Text("Тема", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data class ThemeOption(val key: String, val label: String)
                listOf(
                    ThemeOption("light", "Светлая"),
                    ThemeOption("dark", "Тёмная"),
                    ThemeOption("system", "Системная")
                ).forEach { opt ->
                    FilterChip(
                        selected = theme == opt.key,
                        onClick = { viewModel.setTheme(opt.key) },
                        label = { Text(opt.label) }
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
