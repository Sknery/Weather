package com.example.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.BuildConfig
import com.example.weather.data.ForecastResponse
import com.example.weather.data.WeatherResponse
import com.example.weather.network.RetrofitInstance
import com.example.weather.storage.SharedPrefsManager
import kotlinx.coroutines.launch

enum class UiState {
    LOADING, SUCCESS, ERROR
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPrefsManager(application)

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _forecastData = MutableLiveData<ForecastResponse?>()
    val forecastData: LiveData<ForecastResponse?> = _forecastData

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    fun fetchWeather(city: String) {
        _uiState.value = UiState.LOADING
        viewModelScope.launch {
            try {
                val units = prefsManager.getUnits()
                val weatherResponse = RetrofitInstance.api.getWeather(city, units, BuildConfig.API_KEY)
                val forecastResponse = RetrofitInstance.api.getForecast(city, units, BuildConfig.API_KEY)

                if (weatherResponse.isSuccessful && weatherResponse.body() != null &&
                    forecastResponse.isSuccessful && forecastResponse.body() != null) {
                    _weatherData.postValue(weatherResponse.body())
                    _forecastData.postValue(forecastResponse.body())
                    _uiState.postValue(UiState.SUCCESS)
                    prefsManager.saveLastCity(city)
                } else {
                    _uiState.postValue(UiState.ERROR)
                }
            } catch (e: Exception) {
                _uiState.postValue(UiState.ERROR)
            }
        }
    }

    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        _uiState.value = UiState.LOADING
        viewModelScope.launch {
            try {
                val units = prefsManager.getUnits()
                val weatherResponse = RetrofitInstance.api.getWeatherByCoords(lat, lon, units, BuildConfig.API_KEY)
                val forecastResponse = RetrofitInstance.api.getForecastByCoords(lat, lon, units, BuildConfig.API_KEY)

                if (weatherResponse.isSuccessful && weatherResponse.body() != null &&
                    forecastResponse.isSuccessful && forecastResponse.body() != null) {

                    val responseBody = weatherResponse.body()!!
                    _weatherData.postValue(responseBody)
                    _forecastData.postValue(forecastResponse.body()!!)
                    _uiState.postValue(UiState.SUCCESS)
                    prefsManager.saveLastCity(responseBody.cityName)

                } else {
                    _uiState.postValue(UiState.ERROR)
                }
            } catch (e: Exception) {
                _uiState.postValue(UiState.ERROR)
            }
        }
    }

    fun loadLastCityOrFetchDefault() {
        val lastCity = prefsManager.getLastCity()
        if (lastCity != null) {
            fetchWeather(lastCity)
        } else {
            fetchWeather("London")
        }
    }
}
