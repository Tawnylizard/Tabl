package com.app.tabl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tabl.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsRepository
) : ViewModel() {
    val theme = settings.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_THEME)
}
