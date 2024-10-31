package com.example.waterreminder.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.repository.WaterRepository
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.util.Date

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    private val _weeklyData = MutableLiveData<List<DailyIntake>>()
    val weeklyData: LiveData<List<DailyIntake>> = _weeklyData

    private val _monthlyAverage = MutableLiveData<Int>()
    val monthlyAverage: LiveData<Int> = _monthlyAverage

    init {
        val database = WaterReminderDatabase.getDatabase(application)
        repository = WaterRepository(database.waterLogDao(), database.userSettingsDao())
        loadWeeklyData()
        calculateMonthlyAverage()
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            // Implementation of weekly data calculation
            // This would aggregate the water logs for the past 7 days
        }
    }

    private fun calculateMonthlyAverage() {
        viewModelScope.launch {
            // Implementation of monthly average calculation
        }
    }
}

data class DailyIntake(
    val date: Date,
    val amount: Int
)

class DayAxisValueFormatter : ValueFormatter() {
    private val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    override fun getFormattedValue(value: Float): String {
        return days[value.toInt() % 7]
    }
}