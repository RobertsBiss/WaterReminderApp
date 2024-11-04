package com.example.waterreminder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.waterreminder.data.WaterReminderDatabase
import com.example.waterreminder.databinding.ActivityStartscreenBinding
import com.example.waterreminder.repository.WaterRepository
import com.example.waterreminder.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartscreenBinding
    private lateinit var waterRepository: WaterRepository

    var weight: Double = 0.0
    var gender: String = "unspecified"
    var recommendedIntake: Int = 0
    var measurementSystem: String = "Metric"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        val userSettingsDao = WaterReminderDatabase.getDatabase(this).userSettingsDao()
        val waterLogDao = WaterReminderDatabase.getDatabase(this).waterLogDao()
        waterRepository = WaterRepository(waterLogDao, userSettingsDao)

        // Set initial measurement system based on region
        measurementSystem = if (Locale.getDefault().country == "US") {
            "Imperial"
        } else {
            "Metric"
        }
        // Automatically select the measurement button
        val measurementButtonId = if (measurementSystem == "Metric") {
            R.id.Measurment_select_1
        } else {
            R.id.Measurment_select_2
        }
        binding.MeasurmentToggleGroup.check(measurementButtonId)

        updateWeightHint(measurementSystem)

        // Update weight hint when measurement system changes
        binding.MeasurmentToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val measurementSystem = when (checkedId) {
                    R.id.Measurment_select_1 -> "Metric"
                    R.id.Measurment_select_2 -> "Imperial"
                    else -> "Metric" // Default to Metric
                }
                updateWeightHint(measurementSystem)
                calculateRecommendedIntake() // Recalculate on measurement change
            }
        }

        // Calculate recommended intake when weight changes
        binding.weightTextfield.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateRecommendedIntake()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.buttonFinishsetup.setOnClickListener {
            saveSettingsAndProceed()
        }
    }

    private fun updateWeightHint(measurementSystem: String) {
        val hint = if (measurementSystem == "Metric") {
            "Weight (kg)"
        } else {
            "Weight (lbs)"
        }
        binding.weightTextfield.hint = hint
    }

    private fun calculateRecommendedIntake() {
        val weightText = binding.weightTextfield.text.toString()
        weight = weightText.toDoubleOrNull() ?: 0.0 // Update weight property
        gender = when (binding.GenderToggleGroup.checkedButtonId) {
            R.id.Gender_select_1 -> "Male"
            R.id.Gender_select_2 -> "Female"
            else -> "unspecified"
        } // Update gender property
        val multiplier = if (gender == "Male") 35 else 30
        recommendedIntake = (weight * multiplier).toInt() // Update recommendedIntake property
        binding.recommendedWaterIntake.text = "$recommendedIntake ml"
    }

    private fun saveSettingsAndProceed() {
        val selectedUnit = if (measurementSystem == "Metric") true else false
        val dailyGoal = recommendedIntake
        val remindersEnabled = false // Placeholder for reminder functionality
        val reminderTime = "09:00" // Placeholder for the selected reminder time
        val isDarkTheme = true // Placeholder for dark theme functionality

        val userSettings = UserSettings(
            id = 1, // Primary key for the singleton user settings row
            measurementIsMetric = selectedUnit,
            timeFormat = "24", // Assuming 24-hour format, change if needed
            gender = gender,
            dailyGoal = dailyGoal,
            fixedWaterAmount = 250,
            weight = weight,
            language = Locale.getDefault().language,
            remindersEnabled = remindersEnabled,
            reminderTime = reminderTime,
            isDarkTheme = isDarkTheme
        )

        // Save settings to database
        lifecycleScope.launch {
            waterRepository.insertUserSettings(userSettings)
            // Navigate to the main activity or next screen
            PrefsHelper.setFirstLaunchCompleted(this@SetupActivity)
            startActivity(Intent(this@SetupActivity, MainActivity::class.java))
            finish() // Close SetupActivity
        }
    }


}