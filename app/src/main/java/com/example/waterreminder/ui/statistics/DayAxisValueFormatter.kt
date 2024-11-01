package com.example.waterreminder.ui.statistics

import android.icu.util.Calendar
import androidx.fragment.app.add
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class DayAxisValueFormatter : ValueFormatter() {

    private val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, value.toInt())
        return dateFormat.format(calendar.time)
    }
}