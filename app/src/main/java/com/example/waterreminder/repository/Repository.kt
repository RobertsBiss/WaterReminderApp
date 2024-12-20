package com.example.waterreminder.repository

import androidx.lifecycle.LiveData
import com.example.waterreminder.UserSettings
import com.example.waterreminder.UserSettingsDao
import com.example.waterreminder.data.WaterLog
import com.example.waterreminder.data.WaterLogDao

class WaterRepository(
    private val waterLogDao: WaterLogDao,
    private val userSettingsDao: UserSettingsDao ) {

    val todayLogs: LiveData<List<WaterLog>> = waterLogDao.getTodayLogs()
    val allLogs: LiveData<List<WaterLog>> = waterLogDao.getAllLogs()
    val userSettings: LiveData<UserSettings> = userSettingsDao.getUserSettings()

    suspend fun getUserSettingsDirect(): UserSettings? {
        return userSettingsDao.getDirectUserSettings()
    }

    suspend fun insertUserSettings(settings: UserSettings) {
        userSettingsDao.insert(settings)
    }

    suspend fun insertWaterLog(waterLog: WaterLog) {
        waterLogDao.insert(waterLog)
    }

    suspend fun deleteWaterLog(waterLog: WaterLog) {
        waterLogDao.delete(waterLog)
    }

    suspend fun updateUserSettings(settings: UserSettings) {
        userSettingsDao.update(settings)
    }

    suspend fun getDailyTotal(date: String): Int {
        return waterLogDao.getDailyTotal(date) ?: 0
    }

    suspend fun getDateRangeIntake(startDate: String, endDate: String): List<Int> {
        return waterLogDao.getDateRangeIntake(startDate, endDate)
    }
}