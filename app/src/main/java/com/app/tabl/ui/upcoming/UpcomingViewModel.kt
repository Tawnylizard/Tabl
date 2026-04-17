package com.app.tabl.ui.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.MedicationRepository
import com.app.tabl.domain.model.UpcomingIntake
import com.app.tabl.domain.scheduler.UpcomingIntakeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    val upcomingByDay: StateFlow<Map<LocalDate, List<UpcomingIntake>>> =
        combine(
            repository.getAllMedications(),
            repository.getAllActiveSchedulesFlow()
        ) { medications, schedules ->
            val medMap = medications.associateBy { it.id }
            val today = LocalDate.now()
            val endDate = today.plusDays(30)

            schedules
                .mapNotNull { schedule -> medMap[schedule.medicationId]?.let { med -> med to schedule } }
                .flatMap { (med, schedule) ->
                    UpcomingIntakeCalculator.calculate(med, schedule, today, endDate)
                }
                .sortedBy { it.scheduledAt }
                .groupBy { intake ->
                    LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(intake.scheduledAt),
                        ZoneId.systemDefault()
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
