package com.example.waterreminder.ui.statistics

import android.app.Application
import androidx.lifecycle.*
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.repository.WaterRepository
import com.example.waterreminder.data.DailyIntake
import com.example.waterreminder.data.DailyGoalStatus
import com.example.waterreminder.data.WaterLog
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WaterRepository
    private val _weeklyData = MutableLiveData<List<DailyIntake>>()
    val weeklyData: LiveData<List<DailyIntake>> = _weeklyData

    private val _currentDate = MutableLiveData<Date>()
    val currentDate: LiveData<Date> = _currentDate

    private val _weeklyAverage = MutableLiveData<Int>()
    val weeklyAverage: LiveData<Int> = _weeklyAverage

    private val _weeklyGoals = MutableLiveData<List<DailyGoalStatus>>()
    val weeklyGoals: LiveData<List<DailyGoalStatus>> = _weeklyGoals

    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private var todayLogsObserver: LiveData<List<WaterLog>>? = null

    init {
        val database = WaterReminderDatabase.getDatabase(application)
        repository = WaterRepository(database.waterLogDao(), database.userSettingsDao())
        _currentDate.value = Date()
        setupTodayLogsObserver()
        loadData()
    }

    private fun setupTodayLogsObserver() {
        todayLogsObserver = repository.todayLogs
        todayLogsObserver?.observeForever { _ ->
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = _currentDate.value ?: Date()

            val startDate = getStartOfWeek(calendar.clone() as Calendar)
            val endDate = getEndOfWeek(calendar.clone() as Calendar)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val weekData = mutableListOf<DailyIntake>()

            calendar.time = startDate
            while (!calendar.time.after(endDate)) {
                val dateStr = dateFormat.format(calendar.time)
                val amount = repository.getDailyTotal(dateStr)
                weekData.add(DailyIntake(
                    date = calendar.time,
                    amount = amount,
                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                ))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            _weeklyData.value = weekData.sortedBy { it.dayOfWeek }
            calculateWeeklyAverage()
            loadLast7DaysGoals()
        }
    }

    private fun loadLast7DaysGoals() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val shortDayFormat = SimpleDateFormat("dd\nEEE", Locale.getDefault())

            val goalStatuses = mutableListOf<DailyGoalStatus>()
            val userSettings = repository.getUserSettingsDirect()
            val dailyGoal = userSettings?.dailyGoal ?: 2000

            calendar.add(Calendar.DAY_OF_YEAR, -6)

            repeat(7) {
                val dateStr = dateFormat.format(calendar.time)
                val dayTotal = repository.getDailyTotal(dateStr)

                goalStatuses.add(
                    DailyGoalStatus(
                        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                        date = shortDayFormat.format(calendar.time),
                        achieved = dayTotal >= dailyGoal
                    )
                )
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            _weeklyGoals.value = goalStatuses
        }
    }

    private fun getStartOfWeek(calendar: Calendar): Date {
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return calendar.time
    }

    private fun getEndOfWeek(calendar: Calendar): Date {
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        return calendar.time
    }

    private fun calculateWeeklyAverage() {
        viewModelScope.launch {
            _weeklyAverage.value = weeklyData.value?.let {
                if (it.isEmpty()) 0 else it.sumOf { day -> day.amount } / it.size
            } ?: 0
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

    override fun onCleared() {
        super.onCleared()
        todayLogsObserver?.let {
            it.removeObserver { _ -> }
        }
    }
}