package com.example.waterreminder.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.PrefsHelper
import com.example.waterreminder.UserSettings
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.repository.WaterRepository
import com.example.waterreminder.util.ReminderManager
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    val userSettings: LiveData<UserSettings>
    private val _settingsUpdateEvent = MutableLiveData<SettingsUpdateEvent>()
    val settingsUpdateEvent: LiveData<SettingsUpdateEvent> = _settingsUpdateEvent
    private val reminderManager = ReminderManager(application) // Initialized in SettingsViewModel

    init {
        val database = WaterReminderDatabase.getDatabase(application)
        repository = WaterRepository(database.waterLogDao(), database.userSettingsDao())
        userSettings = repository.userSettings
        ensureDefaultSettings() // Ensures default settings exist
    }

    private fun ensureDefaultSettings() {
        if (PrefsHelper.isFirstLaunch(getApplication())) {
            viewModelScope.launch {
                val currentSettings = repository.getUserSettingsDirect()
                if (currentSettings == null) {
                    val defaultSettings = UserSettings() // Uses defaults from UserSettings class
                    repository.insertUserSettings(defaultSettings)
                    PrefsHelper.setFirstLaunchCompleted(getApplication()) // Mark first launch completed
                }
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
                updateReminders(enabled) // Call to manage reminders
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update reminder settings")
            }
        }
    }

    fun updateReminderTime(time: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(reminderTime = time) // Store time in seconds
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
                updateReminders(true, time) // Pass time in seconds to update reminders
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update reminder time")
            }
        }
    }


//    private fun parseReminderTimeToSeconds(time: String): Int {
//        val (hours, minutes) = time.split(":").map { it.toInt() }
//        return hours * 3600 + minutes * 60
//    }

    fun updateReminders(enabled: Boolean, intervalSeconds: Int = 7200) { // 2 hours default
        if (enabled) {
            reminderManager.scheduleReminder(intervalSeconds)
        } else {
            reminderManager.cancelReminders()
        }
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

    fun updateWeight(weight: Double) {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Updating weight to: $weight") // Log before update
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(weight = weight)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
                Log.d("SettingsViewModel", "Weight updated successfully") // Log after update
                Log.d("SettingsViewModel", "Updated settings: $updatedSettings") // Log updated settings
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update weight")
                Log.e("SettingsViewModel", "Error updating weight", e) // Log error
            }
        }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: return@launch
                val updatedSettings = currentSettings.copy(gender = gender)
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update gender")
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
    }

    fun saveSettingsAndProceed(weight: Double, gender: String, goal: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val currentSettings = userSettings.value ?: UserSettings()
                val updatedSettings = currentSettings.copy(
                    weight = weight,
                    gender = gender,
                    dailyGoal = goal
                )
                repository.updateUserSettings(updatedSettings)
                _settingsUpdateEvent.value = SettingsUpdateEvent.Success
                onComplete()
            } catch (e: Exception) {
                _settingsUpdateEvent.value = SettingsUpdateEvent.Error("Failed to update settings")
            }
        }
    }
}

sealed class SettingsUpdateEvent {
    object Success : SettingsUpdateEvent()
    data class Error(val message: String) : SettingsUpdateEvent()
}