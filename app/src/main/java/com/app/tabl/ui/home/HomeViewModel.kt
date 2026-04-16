package com.app.tabl.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.scheduler.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    object Empty : HomeUiState
    data class Success(val medications: List<Medication>) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getActiveMedications()
        .map { list ->
            if (list.isEmpty()) HomeUiState.Empty
            else HomeUiState.Success(list)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

    fun toggleActive(medication: Medication) {
        viewModelScope.launch {
            repository.setMedicationActive(medication.id, !medication.isActive)
        }
    }

    fun delete(medication: Medication) {
        viewModelScope.launch {
            val schedules = repository.getSchedulesOnce(medication.id)
            schedules.forEach { scheduler.cancel(it.id) }
            repository.deleteMedication(medication)
        }
    }
}
