
package com.example.weather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weather.R
import com.example.weather.databinding.FragmentSettingsBinding
import com.example.weather.storage.SharedPrefsManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: SharedPrefsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = SharedPrefsManager(requireContext())
        setupRadioButtons()
    }

    private fun setupRadioButtons() {
        val currentUnit = prefsManager.getUnits()
        if (currentUnit == "imperial") {
            binding.fahrenheitRadioButton.isChecked = true
        } else {
            binding.celsiusRadioButton.isChecked = true
        }

        binding.unitsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newUnit = when (checkedId) {
                R.id.fahrenheitRadioButton -> "imperial"
                else -> "metric"
            }
            prefsManager.saveUnits(newUnit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
