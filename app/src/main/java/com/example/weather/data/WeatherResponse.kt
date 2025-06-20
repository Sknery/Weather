package com.example.weather.data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val main: Main,
    val wind: Wind,
    val weather: List<Weather>,
    @SerializedName("name") val cityName: String
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Wind(
    val speed: Double
)
data class Weather(
    val main: String,
    val icon: String
)

data class ForecastResponse(
    @SerializedName("list") val forecastList: List<ForecastItem>
)

data class ForecastItem(
    @SerializedName("dt_txt") val dateTime: String,
    val main: Main,
    val weather: List<Weather>
)