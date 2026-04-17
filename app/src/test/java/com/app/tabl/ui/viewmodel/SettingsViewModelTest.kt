package com.app.tabl.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.app.tabl.data.repository.SettingsRepository
import com.app.tabl.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @TempDir
    lateinit var tempDir: File

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var vm: SettingsViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(testDispatcher + Job()),
            produceFile = { File(tempDir, "test_settings.preferences_pb") }
        )
        settingsRepository = SettingsRepository(dataStore)
        vm = SettingsViewModel(settingsRepository)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default snooze is 10 minutes`() = runTest {
        advanceUntilIdle()
        assertEquals(SettingsRepository.DEFAULT_SNOOZE_MINUTES, vm.snoozeMinutes.value)
    }

    @Test
    fun `default maxSnoozeCount is 3`() = runTest {
        advanceUntilIdle()
        assertEquals(SettingsRepository.DEFAULT_MAX_SNOOZE_COUNT, vm.maxSnoozeCount.value)
    }

    @Test
    fun `default theme is system`() = runTest {
        advanceUntilIdle()
        assertEquals(SettingsRepository.DEFAULT_THEME, vm.theme.value)
    }

    @Test
    fun `setSnooze persists new value`() = runTest {
        backgroundScope.launch { vm.snoozeMinutes.collect { } }
        vm.setSnooze(30)
        advanceUntilIdle()
        assertEquals(30, vm.snoozeMinutes.value)
    }

    @Test
    fun `setMaxSnooze persists new value`() = runTest {
        backgroundScope.launch { vm.maxSnoozeCount.collect { } }
        vm.setMaxSnooze(1)
        advanceUntilIdle()
        assertEquals(1, vm.maxSnoozeCount.value)
    }

    @Test
    fun `setMaxRepeat persists new value`() = runTest {
        backgroundScope.launch { vm.maxRepeatCount.collect { } }
        vm.setMaxRepeat(3)
        advanceUntilIdle()
        assertEquals(3, vm.maxRepeatCount.value)
    }

    @Test
    fun `setTheme dark persists dark`() = runTest {
        backgroundScope.launch { vm.theme.collect { } }
        vm.setTheme("dark")
        advanceUntilIdle()
        assertEquals("dark", vm.theme.value)
    }

    @Test
    fun `setTheme light persists light`() = runTest {
        backgroundScope.launch { vm.theme.collect { } }
        vm.setTheme("light")
        advanceUntilIdle()
        assertEquals("light", vm.theme.value)
    }
}
