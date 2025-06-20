// Файл: app/src/main/java/com/example/weather/ui/HomeFragment.kt

package com.example.weather.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.weather.R
import com.example.weather.data.WeatherResponse
import com.example.weather.databinding.FragmentHomeBinding
import com.example.weather.storage.SharedPrefsManager
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var prefsManager: SharedPrefsManager

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        forecastAdapter = ForecastAdapter(emptyList())
        binding.rvForecast.adapter = forecastAdapter

        setupClickListeners()
        observeViewModel()

        if (savedInstanceState == null && viewModel.weatherData.value == null) {
            viewModel.loadLastCityOrFetchDefault()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLastCityOrFetchDefault()
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        binding.btnGetLocation.setOnClickListener { checkLocationPermission() }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadLastCityOrFetchDefault()
        }

        binding.btnChangeCity.setOnClickListener {
            val city = binding.editTextCity.text.toString()
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
                binding.editTextCity.text.clear()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state == UiState.LOADING) View.VISIBLE else View.GONE
            binding.errorText.visibility = if (state == UiState.ERROR) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = state == UiState.LOADING

            val contentVisibility = if (state == UiState.SUCCESS) View.VISIBLE else View.INVISIBLE
            if (binding.holder.visibility != contentVisibility && state == UiState.SUCCESS) {
                binding.holder.visibility = View.VISIBLE
                binding.holder.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in))
            } else {
                binding.holder.visibility = contentVisibility
            }
        }

        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            weatherData?.let { updateUi(it) }
        }

        viewModel.forecastData.observe(viewLifecycleOwner) { forecastResponse ->
            forecastResponse?.let { forecastAdapter.updateData(it.forecastList) }
        }
    }

    private fun updateUi(data: WeatherResponse) {
        val unitSymbol = if (prefsManager.getUnits() == "metric") "°C" else "°F"

        binding.city.text = data.cityName
        binding.temperature.text = "${data.main.temp.toInt()}$unitSymbol"
        binding.humidity.text = "${data.main.humidity}%"
        binding.wind.text = "${data.wind.speed.toInt()} km/h"
        binding.textCondition.text = data.weather.firstOrNull()?.main ?: "Unknown"

        val iconCode = data.weather.firstOrNull()?.icon
        val imageResource = when (iconCode) {
            "01d" -> R.drawable.sun
            "01n" -> R.drawable.moon
            "02d" -> R.drawable.suncloud
            "02n" -> R.drawable.mooncloud
            "03d", "03n", "04d", "04n" -> R.drawable.cloud
            "09d", "09n", "10d", "10n" -> R.drawable.rain
            "11d", "11n" -> R.drawable.storm
            "13d", "13n" -> R.drawable.snow
            "50d", "50n" -> R.drawable.mist
            else -> R.drawable.sun
        }

        val backgroundResource = when (iconCode) {
            "01d", "02d" -> R.drawable.background_sunny
            "01n", "02n" -> R.drawable.background_night
            "03d", "04d", "09d", "10d", "11d", "13d", "50d" -> R.drawable.background_cloudy
            "03n", "04n", "09n", "10n", "11n", "13n", "50n" -> R.drawable.background_night
            else -> R.drawable.background
        }
        binding.main.setBackgroundResource(backgroundResource)
        binding.imgCondition.setImageResource(imageResource)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchWeatherByCoordinates(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(requireContext(), "Cannot get location. Make sure GPS is enabled.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to get location.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
