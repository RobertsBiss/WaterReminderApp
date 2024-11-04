package com.example.waterreminder.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.waterreminder.R
import com.example.waterreminder.databinding.FragmentHomeBinding
import com.example.waterreminder.repository.SettingsManager
import com.example.waterreminder.ui.adapters.WaterLogAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var waterLogAdapter: WaterLogAdapter
    private lateinit var settingsManager: SettingsManager
    private var dailyGoal = 0 // Default daily goal value
    private var fixedAmount = 250 // Default fixed amount of 250 ml for add_water_button2
    private lateinit var konfettiView: KonfettiView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        settingsManager = SettingsManager.getInstance(requireContext())
        konfettiView = binding.konfettiView

        setupRecyclerView()
        setupUI()
        observeData()
        observeDailyGoal()
        observeFixedAmount() // Observe the fixed water amount from SettingsManager

        return binding.root
    }

    private val party by lazy { // Create Party object lazily
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
    }

    override fun onResume() {
        super.onResume()
        observeData()  // Triggers recalculation and UI update on fragment resume
    }

    private fun setupRecyclerView() {
        waterLogAdapter = WaterLogAdapter { waterLog ->
            viewModel.deleteWaterLog(waterLog)
        }
        binding.logRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = waterLogAdapter
        }
    }

    private fun setupUI() {
        binding.addWaterButton.setOnClickListener {
            showAddWaterDialog()
        }

        // Handle single press on add_water_button2 to add the fixed amount
        binding.addWaterButton2.setOnClickListener {
            viewModel.addWaterIntake(fixedAmount)  // Adds the fixed amount of water
        }

        // Handle long press on add_water_button2 to allow the user to change the fixed amount
        binding.addWaterButton2.setOnLongClickListener {
            showSetFixedAmountDialog()  // Opens a dialog to set the fixed amount
            true  // Return true to indicate that the long press was handled
        }
    }

    private fun showAddWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.water_amount_input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Water Intake")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amountText = amountInput.text.toString().trim()
                if (amountText.isNotEmpty()) {
                    try {
                        val amount = amountText.toInt()
                        if (amount > 0) {
                            viewModel.addWaterIntake(amount)
                        } else {
                            Snackbar.make(binding.root, "Please enter a valid amount", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: NumberFormatException) {
                        Snackbar.make(binding.root, "Please enter a valid number", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(binding.root, "Please enter an amount", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Dialog to change the fixed amount for the add_water_button2
    private fun showSetFixedAmountDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.water_amount_input)
        amountInput.setHint("Enter fixed amount")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Fixed Amount")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val amountText = amountInput.text.toString().trim()
                if (amountText.isNotEmpty()) {
                    try {
                        val amount = amountText.toInt()
                        if (amount > 0) {
                            fixedAmount = amount
                            saveFixedAmount(amount)  // Save the fixed amount to settings
                            updateFixedAmountButtonText()  // Update the button text after setting new amount
                        } else {
                            Snackbar.make(binding.root, "Please enter a valid amount", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: NumberFormatException) {
                        Snackbar.make(binding.root, "Please enter a valid number", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(binding.root, "Please enter an amount", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to update button text to display the current fixed amount
    private fun updateFixedAmountButtonText() {
        binding.addWaterButton2.text = "+${fixedAmount} ml"  // Set the button text
    }

    private fun observeData() {
        // Observe water intake logs
        viewModel.todayWaterIntake.observe(viewLifecycleOwner) { logs ->
            val total = logs.sumOf { it.amount }
            updateProgress(total)

            // Update the RecyclerView with the logs
            waterLogAdapter.submitList(logs)
        }
    }

    private fun observeDailyGoal() {
        settingsManager.userSettings.observe(viewLifecycleOwner) { settings ->
            settings?.let {
                dailyGoal = it.dailyGoal
                updateProgress(binding.progressCircle.progress) // Update progress with the new daily goal
            }
        }
    }

    // Observe the fixed amount from SettingsManager
    private fun observeFixedAmount() {
        settingsManager.userSettings.observe(viewLifecycleOwner) { settings ->
            settings?.let {
                fixedAmount = it.fixedWaterAmount
                updateFixedAmountButtonText() // Update the button with the current fixed amount
            }
        }
    }

    private fun saveFixedAmount(amount: Int) {
        lifecycleScope.launch {
            settingsManager.saveFixedWaterAmount(amount)
            // Recalculate progress to avoid the low-number issue after changing fixed amount
            observeData()  // Manually refresh the observed data
        }
    }

    private fun updateProgress(total: Int) {
        // Calculate percentage and update the progress circle
        val percentage = calculateProgress(total)
        binding.progressCircle.progress = percentage

        // Update the text showing how much water has been consumed
        binding.amountText.text = "$total ml"

        // Update the percentage text with the calculated percentage
        binding.percentageText.text = "$percentage%"

        // Display the daily goal like "2000 ml"
        binding.dailydoseText.text = "$dailyGoal ml"

        if (total >= dailyGoal) { // Check if goal is reached and confetti isn't already active
            konfettiView.start(party)
            konfettiView.visibility = View.VISIBLE // Make KonfettiView visible
        } else if (total < dailyGoal) {
            konfettiView.visibility = View.GONE // Hide KonfettiView if goal isn't reached
        }
    }

    private fun calculateProgress(current: Int): Int {
        return if (dailyGoal > 0) {
            ((current.toFloat() / dailyGoal) * 100).toInt()
        } else {
            0 // Avoid division by zero if dailyGoal is not set
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



