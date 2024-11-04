package com.example.waterreminder.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.waterreminder.databinding.FragmentSettingsBinding
import com.example.waterreminder.repository.SettingsManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var settingsManager: SettingsManager

    private var isMetric = true // Track measurement system
    private var genderMultiplier = 35 // Default to male multiplier
    private var hasLoadedInitialSettings = false // Flag to control initial load

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        settingsManager = SettingsManager.getInstance(requireContext())

        setupUI()
        observeSettings()
        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            // Update weight unit label based on metric or imperial system
            unitToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    isMetric = checkedId == binding.metricButton.id
                    updateWeightTextLabel()
                }
            }

            // Update gender multiplier based on selected gender
            genderToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    genderMultiplier = if (checkedId == binding.genderButton1.id) 35 else 30
                }
            }

            // Recalculate daily goal and save all settings when recalculate button is pressed
            recalculateButton.setOnClickListener {
                calculateAndSaveDailyGoal()
            }

            // Set up Logcat button to print weight and daily intake goal
            logcat.setOnClickListener {
                printSettingsToLogcat()
            }
        }
    }

    private fun updateWeightTextLabel() {
        // Update weight label with the appropriate unit based on the measurement system
        val unit = if (isMetric) "kg" else "lb"
        binding.weightTextView.text = "Weight ($unit)"
    }

    private fun calculateAndSaveDailyGoal() {
        val weightText = binding.weightInput.text.toString()
        val weight = weightText.toDoubleOrNull()

        if (weight != null && weight > 0) {
            val adjustedWeight = if (isMetric) weight else weight * 0.453592 // Convert to kg if in lbs
            val dailyGoal = (adjustedWeight * genderMultiplier).toInt()

            // Update UI with calculated goal
            binding.dailyGoalInput.setText(dailyGoal.toString())

            // Save all settings to the database
            lifecycleScope.launch {
                // Save the weight
                settingsManager.updateWeight(weight)

                // Save the daily water intake goal
                settingsManager.updateDailyGoal(dailyGoal)

                // Save the gender
                val gender = if (genderMultiplier == 35) "Male" else "Female"
                settingsManager.updateGender(gender)

                // Save the measurement system
                settingsManager.updateMeasurementSystem(isMetric)

                Log.d("SettingsFragment", "Settings saved - Weight: $weight, Daily Goal: $dailyGoal, Gender: $gender, Measurement System: ${if (isMetric) "Metric" else "Imperial"}")
            }
        } else {
            binding.weightInput.error = "Please enter a valid weight"
        }
    }

    private fun observeSettings() {
        settingsManager.userSettings.observe(viewLifecycleOwner) { settings ->
            settings?.let {
                // Ensure this block only runs on initial load
                if (!hasLoadedInitialSettings) {
                    hasLoadedInitialSettings = true

                    binding.weightInput.setText(it.weight.toString())
                    binding.dailyGoalInput.setText(it.dailyGoal.toString())

                    // Check measurement system
                    isMetric = it.measurementIsMetric
                    binding.unitToggleGroup.check(if (isMetric) binding.metricButton.id else binding.imperialButton.id)

                    // Check gender
                    binding.genderToggleGroup.check(
                        if (it.gender == "Male") binding.genderButton1.id else binding.genderButton2.id
                    )

                    updateWeightTextLabel()
                }
            }
        }
    }

    private fun printSettingsToLogcat() {
        lifecycleScope.launch {
            val settings = settingsManager.getUserSettingsDirect()
            settings?.let {
                Log.d("SettingsFragment", "Weight: ${it.weight} kg")
                Log.d("SettingsFragment", "Daily Water Intake Goal: ${it.dailyGoal} ml")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
