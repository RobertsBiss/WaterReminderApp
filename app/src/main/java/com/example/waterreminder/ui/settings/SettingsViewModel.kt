package com.example.waterreminder.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.UserSettings
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.repository.WaterRepository
import com.example.waterreminder.util.ReminderManager
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    private val reminderManager = ReminderManager(application)
    val userSettings: LiveData<UserSettings>

    private val _settingsUpdateEvent = MutableLiveData<SettingsUpdateEvent>()
    val settingsUpdateEvent: LiveData<SettingsUpdateEvent> = _settingsUpdateEvent

    init {
        val database = WaterReminderDatabase.getDatabase(application)
        repository = WaterRepository(database.waterLogDao(), database.userSettingsDao())
        userSettings = repository.userSettings
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(dailyGoal = goal)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update daily goal")
            }
        }
    }

    fun updateWaterUnit(unit: String) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(waterUnit = unit)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update water unit")
            }
        }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(remindersEnabled = enabled)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update reminder settings")
            }
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(reminderTime = time)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update reminder time")
            }
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(isDarkTheme = isDark)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update theme")
            }
        }

        fun updateSettings(settings: UserSettings) {
            viewModelScope.launch {
                repository.updateUserSettings(settings)
            }
        }

        fun updateReminders(enabled: Boolean, intervalHours: Int = 2) {
            if (enabled) {
                reminderManager.scheduleReminder(intervalHours)
            } else {
                reminderManager.cancelReminders()
            }
        }
    }
}

sealed class SettingsUpdateEvent {
    object Success : SettingsUpdateEvent()
    data class Error(val message: String) : SettingsUpdateEvent()
}