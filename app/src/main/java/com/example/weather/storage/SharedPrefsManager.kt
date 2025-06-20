package com.example.weather.storage

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_CITY = "last_city"
        private const val KEY_UNITS = "units" // Новая константа
    }

    fun saveLastCity(city: String) {
        prefs.edit().putString(KEY_LAST_CITY, city).apply()
    }

    fun getLastCity(): String? {
        return prefs.getString(KEY_LAST_CITY, null)
    }

    fun saveUnits(units: String) {
        prefs.edit().putString(KEY_UNITS, units).apply()
    }

    fun getUnits(): String {
        // По умолчанию - метрическая система (Цельсий)
        return prefs.getString(KEY_UNITS, "metric") ?: "metric"
    }
}