package com.app.tabl.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.model.Schedule
import com.app.tabl.domain.scheduler.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    object Empty : HomeUiState
    data class Success(val medications: List<Medication>) : HomeUiState
}

data class ExpiringScheduleInfo(
    val medication: Medication,
    val schedule: Schedule
)

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

    val expiringSchedules: StateFlow<List<ExpiringScheduleInfo>> =
        combine(
            repository.getAllMedications(),
            repository.getExpiringSchedules(withinDays = 7)
        ) { medications, schedules ->
            val medMap = medications.associateBy { it.id }
            schedules.mapNotNull { schedule ->
                medMap[schedule.medicationId]?.let { med ->
                    ExpiringScheduleInfo(med, schedule)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun extendSchedule(info: ExpiringScheduleInfo, days: Int = 30) {
        viewModelScope.launch {
            repository.extendScheduleEndDate(info.schedule, days)
            scheduler.scheduleNext(info.medication.id, info.schedule.id)
        }
    }
}
