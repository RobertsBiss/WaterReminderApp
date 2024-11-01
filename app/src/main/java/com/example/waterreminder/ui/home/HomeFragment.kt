package com.example.waterreminder.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.waterreminder.R
import com.example.waterreminder.databinding.FragmentHomeBinding
import com.example.waterreminder.ui.adapters.WaterLogAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var waterLogAdapter: WaterLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupUI()
        observeData()

        return binding.root
    }

    private fun setupRecyclerView() {
        waterLogAdapter = WaterLogAdapter { waterLog ->
            // Optional: Implement delete functionality if needed
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
        viewModel.todayWaterIntake.observe(viewLifecycleOwner) { logs ->
            val total = logs.sumOf { it.amount }
            updateProgress(total)

            // Update the RecyclerView with the logs
            waterLogAdapter.submitList(logs)
        }
    }

    private fun updateProgress(total: Int) {
        binding.progressCircle.progress = calculateProgress(total)
        binding.amountText.text = "$total ml"
    }

    private fun calculateProgress(current: Int): Int {
        return ((current.toFloat() / viewModel.dailyGoal) * 100).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}