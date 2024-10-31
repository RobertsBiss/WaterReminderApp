package com.example.waterreminder.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.waterreminder.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupUI()
        observeData()

        return binding.root
    }

    private fun setupUI() {
        binding.addWaterButton.setOnClickListener {
            viewModel.addWaterIntake(100) // Default 100ml
        }
    }

    private fun observeData() {
        viewModel.todayWaterIntake.observe(viewLifecycleOwner) { logs ->
            val total = logs.sumOf { it.amount }
            updateProgress(total)
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