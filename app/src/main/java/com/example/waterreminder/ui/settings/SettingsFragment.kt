package com.example.waterreminder.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.waterreminder.databinding.FragmentSettingsBinding
import com.example.waterreminder.util.ReminderManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var reminderManager: ReminderManager // Initialize ReminderManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        reminderManager = ReminderManager(requireContext()) // Instantiate ReminderManager

        setupUI()
        observeSettings()
        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            dailyGoalInput.setOnEditorActionListener { _, _, _ ->
                updateDailyGoal()
                true
            }

            reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showTimePickerDialog()
                    reminderManager.scheduleReminder(2) // Schedule reminder every 2 hours
                } else {
                    reminderManager.cancelReminders() // Cancel reminders if turned off
                }
                viewModel.updateReminderEnabled(isChecked)
            }

            unitToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    viewModel.updateWaterUnit(
                        when (checkedId) {
                            binding.mlButton.id -> "ml"
                            binding.ozButton.id -> "oz"
                            else -> "ml"
                        }
                    )
                }
            }

            themeSwitch.setOnCheckedChangeListener { _, isDark ->
                viewModel.updateTheme(isDark)
            }
        }
    }

    private fun observeSettings() {
        viewModel.userSettings.observe(viewLifecycleOwner) { settings ->
            if (settings != null) {  // Check for null
                binding.apply {
                    dailyGoalInput.setText(String.format(Locale.getDefault(), "%d", settings.dailyGoal))
                    reminderSwitch.isChecked = settings.remindersEnabled
                    reminderTimeText.text = settings.reminderTime

                    when (settings.waterUnit) {
                        "ml" -> unitToggleGroup.check(binding.mlButton.id) // Check ml button
                        "oz" -> unitToggleGroup.check(binding.ozButton.id) // Check oz button
                    }

                    themeSwitch.isChecked = settings.isDarkTheme
                }
            } else {
                // Handle the case where settings is null
                binding.apply {
                    dailyGoalInput.setText("2000") // Set default value for daily goal
                    reminderSwitch.isChecked = false // Default to reminders disabled
                    reminderTimeText.text = "09:00" // Default reminder time
                    unitToggleGroup.check(binding.mlButton.id) // Default water unit
                    themeSwitch.isChecked = false // Default to light theme
                }
            }
        }

        viewModel.settingsUpdateEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is SettingsUpdateEvent.Success -> showSuccessMessage()
                is SettingsUpdateEvent.Error -> showErrorMessage(event.message)
            }
        }
    }

    private fun showTimePickerDialog() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(9)
            .setMinute(0)
            .setTitleText("Set Reminder Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
            viewModel.updateReminderTime(timeString)
        }

        picker.show(parentFragmentManager, "time_picker")
    }

    private fun updateDailyGoal() {
        val goal = binding.dailyGoalInput.text.toString().toIntOrNull()
        if (goal != null && goal > 0) {
            viewModel.updateDailyGoal(goal)
        } else {
            binding.dailyGoalInput.error = "Please enter a valid goal"
        }
    }

    private fun showSuccessMessage() {
        // Show success message using Snackbar or Toast
    }

    private fun showErrorMessage(message: String) {
        // Show error message using Snackbar or Toast
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}