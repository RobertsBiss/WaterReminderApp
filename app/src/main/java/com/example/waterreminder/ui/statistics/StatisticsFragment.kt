package com.example.waterreminder.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.waterreminder.R
import com.example.waterreminder.databinding.FragmentStatisticsBinding
import com.example.waterreminder.data.DailyIntake
import com.example.waterreminder.data.DailyGoalStatus
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels()
    private val goalViews = mutableListOf<View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        setupChart()
        setupNavigation()
        setupGoalViews()
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

    private fun setupGoalViews() {
        goalViews.apply {
            add(binding.sundayGoal.root)
            add(binding.mondayGoal.root)
            add(binding.tuesdayGoal.root)
            add(binding.wednesdayGoal.root)
            add(binding.thursdayGoal.root)
            add(binding.fridayGoal.root)
            add(binding.saturdayGoal.root)
        }
    }

    private fun observeData() {
        viewModel.weeklyData.observe(viewLifecycleOwner) { weeklyData ->
            updateChart(weeklyData)
        }

        viewModel.weeklyAverage.observe(viewLifecycleOwner) { average ->
            binding.weeklyAverageText.text = "Weekly Average: ${average}ml"
        }

        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.monthYearText.text = viewModel.getFormattedDate()
        }

        viewModel.weeklyGoals.observe(viewLifecycleOwner) { goalStatuses ->
            updateGoalStatuses(goalStatuses)
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

    private fun updateGoalStatuses(goalStatuses: List<DailyGoalStatus>) {
        goalStatuses.forEachIndexed { index, status ->
            val goalView = goalViews[index]

            goalView.findViewById<TextView>(R.id.dayText).text = status.date
            goalView.findViewById<ImageView>(R.id.goalStatusIcon).setImageResource(
                if (status.achieved) R.drawable.ic_trophy else R.drawable.ic_goal_empty
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}