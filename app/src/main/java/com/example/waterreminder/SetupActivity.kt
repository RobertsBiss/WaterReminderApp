package com.example.waterreminder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.waterreminder.databinding.ActivityStartscreenBinding
import kotlinx.coroutines.launch

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartscreenBinding
    // Assuming you have access to your UserSettingsDao instance
    private lateinit var userSettingsDao: UserSettingsDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ... (Get userSettingsDao instance - e.g., from your database) ...

        binding.buttonFinishsetup.setOnClickListener {
            saveSettingsAndProceed()
        }
    }

    private fun saveSettingsAndProceed() {
        val weightText = binding.weightTextfield.text.toString()
        val gender = when (binding.GenderToggleGroup.checkedButtonId) {
            R.id.Gender_select_1 -> "Male"
            R.id.Gender_select_2 -> "Female"
            else -> "unspecified" // Default to unspecified if not selected
        }
        val weightUnit = when (binding.MeasurmentToggleGroup.checkedButtonId) {
            R.id.Measurment_select_1 -> "kg" // Metric
            R.id.Measurment_select_2 -> "lbs" // Imperial
            else -> "kg" // Default to kg if not selected
        }

        // Validate input (e.g., check if weight is empty or invalid)
        if (weightText.isEmpty()) {
            Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show()
            return
        }

        // Create UserSettings object
        val userSettings = UserSettings(
            id = 1, // Assuming you have a single user settings entry
            gender = gender,
            weightUnit = weightUnit,
            // ... other settings ...
            dailyGoal = weightText.toIntOrNull() ?: 2000 // Default to 2000 if invalid
        )

        // Store settings in Room database using coroutines
        lifecycleScope.launch {
            userSettingsDao.insert(userSettings)
        }

        // Proceed to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}