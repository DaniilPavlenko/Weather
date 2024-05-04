package ru.dpav.weather.ui

import androidx.annotation.DrawableRes
import ru.dpav.weather.R

internal object WeatherIconAssociator {
    @DrawableRes
    fun getIconByName(weatherIconName: String) = when (weatherIconName) {
        "01d" -> R.drawable.weather_icon_01
        "01n" -> R.drawable.weather_icon_01n
        "02d" -> R.drawable.weather_icon_02
        "02n" -> R.drawable.weather_icon_02n
        "03d", "03n" -> R.drawable.weather_icon_03
        "04d", "04n" -> R.drawable.weather_icon_04
        "09d", "09n" -> R.drawable.weather_icon_09
        "10d" -> R.drawable.weather_icon_10
        "10n" -> R.drawable.weather_icon_10n
        "11d", "11n" -> R.drawable.weather_icon_11
        "13d", "13n" -> R.drawable.weather_icon_13
        "50d" -> R.drawable.weather_icon_50
        else -> R.drawable.weather_icon_01
    }
}
