package com.app.tabl.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.LogRepository
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.model.MedicationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HistoryUiState(
    val medications: List<Medication> = emptyList(),
    val selectedMedicationId: Long? = null,
    val logs: List<MedicationLog> = emptyList(),
    val compliancePercent: Int = 0,
    val periodDays: Int = 7
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _selectedMedId = MutableStateFlow<Long?>(null)
    private val _periodDays = MutableStateFlow(7)

    val medications: StateFlow<List<Medication>> = medicationRepository.getAllMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<MedicationLog>> = combine(_selectedMedId, _periodDays) { medId, days ->
        medId to days
    }.flatMapLatest { (medId, days) ->
        val to = System.currentTimeMillis()
        val from = LocalDate.now().minusDays(days.toLong())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (medId != null) {
            logRepository.getLogsForMedication(medId, from, to)
        } else {
            logRepository.getAllLogs(from, to)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val compliancePercent: StateFlow<Int> = combine(_selectedMedId, _periodDays) { medId, days ->
        medId to days
    }.flatMapLatest { (medId, days) ->
        flow {
            if (medId == null) { emit(0); return@flow }
            val to = System.currentTimeMillis()
            val from = LocalDate.now().minusDays(days.toLong())
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            emit(logRepository.getCompliancePercent(medId, from, to))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val selectedMedicationId: StateFlow<Long?> = _selectedMedId.asStateFlow()
    val periodDays: StateFlow<Int> = _periodDays.asStateFlow()

    fun selectMedication(id: Long?) { _selectedMedId.value = id }
    fun setPeriod(days: Int) { _periodDays.value = days }
}
