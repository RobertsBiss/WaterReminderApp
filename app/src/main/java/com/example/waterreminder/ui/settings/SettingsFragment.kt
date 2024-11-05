package com.example.waterreminder.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.waterreminder.R
import com.example.waterreminder.databinding.FragmentSettingsBinding
import com.example.waterreminder.repository.SettingsManager
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.waterreminder.util.ReminderManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var settingsManager: SettingsManager

    private var isMetric = true // Track measurement system
    private var genderMultiplier = 35 // Default to male multiplier
    private var hasLoadedInitialSettings = false // Flag to control initial load

    // Reminder-related variables
    private lateinit var reminderTimePickerDialog: TimePickerDialog
    private var reminderTimeInSeconds: Int = 0

    private lateinit var reminderManager: ReminderManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        settingsManager = SettingsManager.getInstance(requireContext())
        reminderManager = ReminderManager(requireContext())

        setupUI()
        observeSettings()

        //Get reminderTime using SettingsManager and print it out in Logcat
        lifecycleScope.launch {
            val settings = settingsManager.getUserSettingsDirect()
            settings?.let {
                Log.d("SettingsFragment", "Reminder Time: ${it.reminderTime}")
                Log.d("SettingsFragment", "Reminders Enabled: ${it.remindersEnabled}")
            }
        }

//        // Set up reminder switch listener
//        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.updateReminderEnabled(isChecked)
//            if (isChecked) {
//                showReminderTimePickerDialog()
//            } else {
//                viewModel.updateReminderTime("")
//                binding.reminderTimeText.text = ""
//            }
//        }

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

            binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    lifecycleScope.launch {
                        settingsManager.updateRemindersEnabled(true)
                    }
                } else {
                    lifecycleScope.launch {
                        settingsManager.updateRemindersEnabled(false)
                    }
                    reminderManager.cancelReminders() // Cancel reminders
                }
            }

            // Button to show the reminder interval options
            binding.reminderShowTimesButton.setOnClickListener {
                showReminderIntervalDialog()
            }

            // Recalculate daily goal and save all settings when recalculate button is pressed
            recalculateButton.setOnClickListener {
                calculateAndSaveDailyGoal()
            }
        }
    }

    private fun showReminderIntervalDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reminder_interval, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.reminderRadioGroup)
        val customInput = dialogView.findViewById<EditText>(R.id.customMinutesInput)

        // Predefine intervals using radio button IDs as keys
        val predefinedIntervals = mapOf(
            R.id.radio_5 to 5,   // 5 minutes
            R.id.radio_10 to 10, // 10 minutes
            R.id.radio_30 to 30  // 30 minutes
        )

        // Create an AlertDialog to show the options
        AlertDialog.Builder(requireContext())
            .setTitle("Set Reminder Interval")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                // Check which radio button was selected
                val selectedRadioButtonId = radioGroup.checkedRadioButtonId
                if (selectedRadioButtonId != -1) {
                    // Get the predefined interval from the selected radio button
                    val selectedInterval = predefinedIntervals[selectedRadioButtonId] ?: 0
                    Log.d("SettingsFragment", "Selected interval: $selectedInterval minutes")

                    // Schedule the reminder with the selected interval (converted to seconds)
                    scheduleReminder(selectedInterval * 60)
                } else {
                    // If no radio button selected, use the custom input
                    val customMinutes = customInput.text.toString().toIntOrNull() ?: 0
                    if (customMinutes > 0) {
                        Log.d("SettingsFragment", "Custom interval: $customMinutes minutes")
                        scheduleReminder(customMinutes * 60) // Convert custom minutes to seconds
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun scheduleReminder(intervalInSeconds: Int) {
        if (intervalInSeconds > 0) {
            //Update reminderTime using SettingsManager
            lifecycleScope.launch {
                settingsManager.updateReminderTime(intervalInSeconds)
            }
            reminderManager.scheduleReminder(intervalInSeconds) // Schedule with custom interval
        }
    }

//    private fun showReminderTimePickerDialog() {
//        val currentTime = Calendar.getInstance()
//        reminderTimePickerDialog = TimePickerDialog(
//            requireContext(),
//            { _, hourOfDay, minute ->
//                val reminderTime = String.format("%02d:%02d", hourOfDay, minute)
//                binding.reminderTimeText.text = reminderTime
//                viewModel.updateReminderTime(reminderTime)
//                reminderTimeInSeconds = hourOfDay * 3600 + minute * 60
//            },
//            currentTime.get(Calendar.HOUR_OF_DAY),
//            currentTime.get(Calendar.MINUTE),
//            true
//        )
//        reminderTimePickerDialog.show()
//    }

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
                    binding.reminderSwitch.isChecked = it.remindersEnabled
                    binding.reminderTimeText.text = "Remind me every ${it.reminderTime/60} minutes"

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