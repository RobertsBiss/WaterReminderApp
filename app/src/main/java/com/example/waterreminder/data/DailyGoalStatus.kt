package com.example.waterreminder.data

data class DailyGoalStatus(
    val dayOfWeek: Int,
    val date: String,
    val achieved: Boolean
)