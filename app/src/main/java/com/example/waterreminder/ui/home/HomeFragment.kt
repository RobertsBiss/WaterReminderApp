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

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var waterLogAdapter: WaterLogAdapter
    private lateinit var settingsManager: SettingsManager
    private var dailyGoal = 0 // Default daily goal value

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        settingsManager = SettingsManager.getInstance(requireContext())

        setupRecyclerView()
        setupUI()
        observeData()
        observeDailyGoal() // Observe daily goal from SettingsManager

        return binding.root
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

    private fun observeData() {
        // Observe water intake logs
        viewModel.todayWaterIntake.observe(viewLifecycleOwner) { logs ->
            val total = logs.sumOf { it.amount }
            updateProgress(total)

            // Update the RecyclerView with the logs
            waterLogAdapter.submitList(logs)
        }
    }

    // Optionally, force an update on fragment creation by calling observeData again
    override fun onResume() {
        super.onResume()
        observeData()  // Triggers recalculation and UI update on fragment resume
    }

    private fun observeDailyGoal() {
        settingsManager.userSettings.observe(viewLifecycleOwner) { settings ->
            settings?.let {
                dailyGoal = it.dailyGoal
                updateProgress(binding.progressCircle.progress) // Update progress with the new daily goal
            }
        }
    }

    private fun updateProgress(total: Int) {
        binding.progressCircle.progress = calculateProgress(total)
        binding.amountText.text = "$total ml"
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
