package com.example.weather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.data.ForecastItem
import com.example.weather.databinding.ForecastItemBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ForecastAdapter(private var forecastItems: List<ForecastItem>) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(val binding: ForecastItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = forecastItems[position]
        val binding = holder.binding

        binding.tvTemp.text = "${item.main.temp.toInt()}°"

        val description = item.weather.firstOrNull()?.main ?: ""
        binding.tvDescription.text = description

        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val outputFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH)
            val dateTime = LocalDateTime.parse(item.dateTime, inputFormatter)
            binding.tvDate.text = dateTime.format(outputFormatter).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            binding.tvDate.text = item.dateTime
        }

        val iconCode = item.weather.firstOrNull()?.icon
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
        binding.ivWeatherIcon.setImageResource(imageResource)
    }

    override fun getItemCount(): Int = forecastItems.size

    fun updateData(newForecastItems: List<ForecastItem>) {
        this.forecastItems = newForecastItems
        notifyDataSetChanged()
    }
}