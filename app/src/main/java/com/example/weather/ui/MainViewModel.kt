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
import com.example.weather.storage.cache.WeatherCacheEntity
import com.example.weather.storage.cache.WeatherDatabase
import com.google.gson.Gson
import kotlinx.coroutines.launch

enum class UiState {
    LOADING, SUCCESS, ERROR
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPrefsManager(application)
    private val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
    private val gson = Gson()

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _forecastData = MutableLiveData<ForecastResponse?>()
    val forecastData: LiveData<ForecastResponse?> = _forecastData

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val CACHE_DURATION_MS = 15 * 60 * 1000

    fun fetchWeather(city: String) {
        _uiState.value = UiState.LOADING

        viewModelScope.launch {
            val cachedData = weatherDao.getCacheForCity(city)
            val isCacheStale = (System.currentTimeMillis() - (cachedData?.timestamp ?: 0)) > CACHE_DURATION_MS

            if (cachedData != null && !isCacheStale) {
                _weatherData.postValue(gson.fromJson(cachedData.weatherResponseJson, WeatherResponse::class.java))
                _forecastData.postValue(gson.fromJson(cachedData.forecastResponseJson, ForecastResponse::class.java))
                _uiState.postValue(UiState.SUCCESS)
            } else {
                fetchFromNetwork(city)
            }
        }
    }

    private suspend fun fetchFromNetwork(city: String) {
        try {
            val units = prefsManager.getUnits()
            val weatherResponse = RetrofitInstance.api.getWeather(city, units, BuildConfig.API_KEY)
            val forecastResponse = RetrofitInstance.api.getForecast(city, units, BuildConfig.API_KEY)

            if (weatherResponse.isSuccessful && weatherResponse.body() != null &&
                forecastResponse.isSuccessful && forecastResponse.body() != null) {

                val weatherBody = weatherResponse.body()!!
                val forecastBody = forecastResponse.body()!!

                _weatherData.postValue(weatherBody)
                _forecastData.postValue(forecastBody)
                _uiState.postValue(UiState.SUCCESS)

                val cacheEntity = WeatherCacheEntity(
                    cityName = weatherBody.cityName,
                    weatherResponseJson = gson.toJson(weatherBody),
                    forecastResponseJson = gson.toJson(forecastBody),
                    timestamp = System.currentTimeMillis()
                )
                weatherDao.insertOrUpdate(cacheEntity)

                prefsManager.saveLastCity(weatherBody.cityName)

            } else {
                _uiState.postValue(UiState.ERROR)
            }
        } catch (e: Exception) {
            _uiState.postValue(UiState.ERROR)
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

                    val weatherBody = weatherResponse.body()!!
                    val forecastBody = forecastResponse.body()!!

                    _weatherData.postValue(weatherBody)
                    _forecastData.postValue(forecastBody)
                    _uiState.postValue(UiState.SUCCESS)

                    val cacheEntity = WeatherCacheEntity(
                        cityName = weatherBody.cityName,
                        weatherResponseJson = gson.toJson(weatherBody),
                        forecastResponseJson = gson.toJson(forecastBody),
                        timestamp = System.currentTimeMillis()
                    )
                    weatherDao.insertOrUpdate(cacheEntity)
                    prefsManager.saveLastCity(weatherBody.cityName)

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