package com.app.tabl.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.Medication
import com.app.tabl.domain.model.Schedule
import com.app.tabl.domain.scheduler.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddMedicationUiState(
    val name: String = "",
    val dose: String = "",
    val colorIndex: Int = 0,
    val stockCount: String = "",
    val stockThreshold: String = "",
    val timeHour: Int = 9,
    val timeMinute: Int = 0,
    val daysOfWeek: Set<Int> = emptySet(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(AddMedicationUiState())
    val state: StateFlow<AddMedicationUiState> = _state.asStateFlow()

    fun loadForEdit(medicationId: Long) {
        viewModelScope.launch {
            val med = repository.getMedicationById(medicationId) ?: return@launch
            val schedules = repository.getSchedulesOnce(medicationId)
            val first = schedules.firstOrNull()
            _state.value = AddMedicationUiState(
                name = med.name,
                dose = med.dose ?: "",
                colorIndex = med.colorIndex,
                stockCount = med.stockCount?.toString() ?: "",
                stockThreshold = med.stockThreshold?.toString() ?: "",
                timeHour = first?.timeHour ?: 9,
                timeMinute = first?.timeMinute ?: 0,
                daysOfWeek = first?.daysOfWeek ?: emptySet(),
                startDate = first?.startDate,
                endDate = first?.endDate
            )
        }
    }

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, nameError = null) }
    fun onDoseChange(value: String) { _state.value = _state.value.copy(dose = value) }
    fun onColorChange(index: Int) { _state.value = _state.value.copy(colorIndex = index) }
    fun onStockCountChange(value: String) { _state.value = _state.value.copy(stockCount = value) }
    fun onStockThresholdChange(value: String) { _state.value = _state.value.copy(stockThreshold = value) }
    fun onTimeChange(hour: Int, minute: Int) { _state.value = _state.value.copy(timeHour = hour, timeMinute = minute) }
    fun onDayToggle(day: Int) {
        val days = _state.value.daysOfWeek.toMutableSet()
        if (day in days) days.remove(day) else days.add(day)
        _state.value = _state.value.copy(daysOfWeek = days)
    }
    fun onStartDateChange(date: LocalDate?) { _state.value = _state.value.copy(startDate = date) }
    fun onEndDateChange(date: LocalDate?) { _state.value = _state.value.copy(endDate = date) }

    fun save(existingMedicationId: Long? = null) {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(nameError = "Введите название лекарства")
            return
        }

        _state.value = s.copy(isSaving = true)

        viewModelScope.launch {
            val medication = Medication(
                id = existingMedicationId ?: 0,
                name = s.name.trim(),
                dose = s.dose.trim().ifBlank { null },
                colorIndex = s.colorIndex,
                stockCount = s.stockCount.trim().toIntOrNull(),
                stockThreshold = s.stockThreshold.trim().toIntOrNull()
            )

            val schedule = Schedule(
                medicationId = existingMedicationId ?: 0,
                timeHour = s.timeHour,
                timeMinute = s.timeMinute,
                daysOfWeek = s.daysOfWeek,
                startDate = s.startDate,
                endDate = s.endDate
            )

            if (existingMedicationId != null) {
                repository.updateMedication(medication)
                val old = repository.getSchedulesOnce(existingMedicationId)
                old.forEach { scheduler.cancel(it.id) }
                val scheduleId = repository.saveSchedule(schedule.copy(medicationId = existingMedicationId))
                scheduler.scheduleNext(existingMedicationId, scheduleId)
            } else {
                val medId = repository.saveMedication(medication, listOf(schedule))
                val schedules = repository.getSchedulesOnce(medId)
                schedules.forEach { scheduler.scheduleNext(medId, it.id) }
            }

            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}
