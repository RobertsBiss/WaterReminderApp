package com.example.waterreminder.ui.statistics

import com.github.mikephil.charting.formatter.ValueFormatter

class DayAxisValueFormatter : ValueFormatter() {
    private val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    override fun getFormattedValue(value: Float): String {
        return days[value.toInt() % 7]
    }
}