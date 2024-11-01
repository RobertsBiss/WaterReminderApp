package com.example.waterreminder.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.WaterLog
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.repository.WaterRepository
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    val todayWaterIntake: LiveData<List<WaterLog>>
    var dailyGoal = 2000 // Default 2L

    init {
        val database = WaterReminderDatabase.getDatabase(application)
        repository = WaterRepository(database.waterLogDao(), database.userSettingsDao())
        todayWaterIntake = repository.todayLogs
    }

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            val now = Date()
            repository.insertWaterLog(WaterLog(amount = amount, timestamp = now))
        }
    }

    fun deleteWaterLog(waterLog: WaterLog) {
        viewModelScope.launch {
            repository.deleteWaterLog(waterLog)
        }
    }
}