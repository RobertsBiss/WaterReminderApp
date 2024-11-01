package com.example.waterreminder.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.repository.WaterRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.waterreminder.data.DailyIntake

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    private val _weeklyData = MutableLiveData<List<DailyIntake>>()
    val weeklyData: LiveData<List<DailyIntake>> = _weeklyData

    private val _currentDate = MutableLiveData<Date>()
    val currentDate: LiveData<Date> = _currentDate

    private val _monthlyAverage = MutableLiveData<Int>()
    val monthlyAverage: LiveData<Int> = _monthlyAverage

    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    init {
        val database = WaterReminderDatabase.getDatabase(application)
        repository = WaterRepository(database.waterLogDao(), database.userSettingsDao())
        _currentDate.value = Date()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = _currentDate.value ?: Date()

            // Set to start of the week
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startDate = calendar.time

            // Move to end of week
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endDate = calendar.time

            // Here you would fetch data from your repository for the date range
            // For now, we'll simulate some data
            val weekData = (0..6).map { dayOffset ->
                calendar.time = startDate
                calendar.add(Calendar.DAY_OF_MONTH, dayOffset)
                DailyIntake(
                    date = calendar.time,
                    amount = (500..2500).random() // Replace with actual data from repository
                )
            }
            _weeklyData.value = weekData
            calculateMonthlyAverage()
        }
    }

    fun navigateWeek(forward: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value ?: Date()
        calendar.add(Calendar.WEEK_OF_YEAR, if (forward) 1 else -1)
        _currentDate.value = calendar.time
        loadData()
    }

    fun getFormattedDate(): String {
        return dateFormat.format(_currentDate.value ?: Date())
    }

    private fun calculateMonthlyAverage() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = _currentDate.value ?: Date()

            // Calculate monthly average (implement actual calculation based on your data)
            _monthlyAverage.value = weeklyData.value?.let { it.sumOf { day -> day.amount } / it.size } ?: 0
        }
    }
}