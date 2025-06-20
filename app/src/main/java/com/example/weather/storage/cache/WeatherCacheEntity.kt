package com.example.weather.storage.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val cityName: String, // Название города будет уникальным ключом
    val weatherResponseJson: String, // JSON-строка для ответа о текущей погоде
    val forecastResponseJson: String, // JSON-строка для ответа с прогнозом
    val timestamp: Long // Время сохранения кэша
)