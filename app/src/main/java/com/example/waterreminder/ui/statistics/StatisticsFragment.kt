package com.example.waterreminder.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.waterreminder.R
import com.example.waterreminder.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.waterreminder.data.DailyIntake


class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        setupChart()
        setupNavigation()
        observeData()
        return binding.root
    }

    private fun setupChart() {
        binding.weeklyChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = DayAxisValueFormatter()
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(500)
        }
    }

    private fun setupNavigation() {
        binding.previousMonthButton.setOnClickListener {
            viewModel.navigateWeek(false)
        }

        binding.nextMonthButton.setOnClickListener {
            viewModel.navigateWeek(true)
        }
    }

    private fun observeData() {
        viewModel.weeklyData.observe(viewLifecycleOwner) { weeklyData ->
            updateChart(weeklyData)
        }

        viewModel.monthlyAverage.observe(viewLifecycleOwner) { average ->
            binding.monthlyAverageText.text = "Monthly Average: ${average}ml"
        }

        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.monthYearText.text = viewModel.getFormattedDate()
        }
    }

    private fun updateChart(data: List<DailyIntake>) {
        val entries = data.mapIndexed { index, dailyIntake ->
            BarEntry(index.toFloat(), dailyIntake.amount.toFloat())
        }

        val dataSet = BarDataSet(entries, "Water Intake (ml)").apply {
            color = resources.getColor(R.color.holo_blue_light, null)
            valueTextSize = 12f
        }

        binding.weeklyChart.apply {
            this.data = BarData(dataSet)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}