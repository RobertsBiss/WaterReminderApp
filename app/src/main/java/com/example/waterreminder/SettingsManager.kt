package com.example.waterreminder.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.waterreminder.UserSettings
import com.example.waterreminder.data.WaterReminderDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsManager private constructor(context: Context) {

    private val userSettingsDao = WaterReminderDatabase.getDatabase(context).userSettingsDao()

    // Function to get LiveData for user settings
    val userSettings: LiveData<UserSettings> = userSettingsDao.getUserSettings()

    // Fetch user settings directly
    suspend fun getUserSettingsDirect(): UserSettings? = withContext(Dispatchers.IO) {
        userSettingsDao.getDirectUserSettings()
    }

    // Function to update the weight
    suspend fun updateWeight(weight: Double) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(weight = weight))
        }
    }

    // Function to update the daily water intake goal
    suspend fun updateDailyGoal(dailyGoal: Int) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(dailyGoal = dailyGoal))
        }
    }

    // Function to update gender
    suspend fun updateGender(gender: String) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(gender = gender))
        }
    }
    // Function to change between metric or imperial
    suspend fun updateMeasurementSystem(metric: Boolean) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(measurementIsMetric = metric))
        }
    }

    suspend fun saveFixedWaterAmount(amount: Int) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(fixedWaterAmount = amount))
        }
    }

    suspend fun updateReminderTime(time: Int) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(reminderTime = time))
        }
    }

    // Function to enable or disable reminders
    suspend fun updateRemindersEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val settings = getUserSettingsDirect()
        if (settings != null) {
            userSettingsDao.update(settings.copy(remindersEnabled = enabled))
        }
    }

    // Other similar functions for different settings
    //...

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
