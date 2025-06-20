package com.example.weather.storage.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    // При вставке данных для города, который уже есть, старые данные заменятся
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cache: WeatherCacheEntity)

    // Получение кэша для конкретного города
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName")
    suspend fun getCacheForCity(cityName: String): WeatherCacheEntity?
}