package ru.dpav.weather.ui

import androidx.annotation.DrawableRes
import ru.dpav.core.model.WeatherType
import ru.dpav.core.model.WeatherType.CLEAR
import ru.dpav.core.model.WeatherType.CLOUDY
import ru.dpav.core.model.WeatherType.DRIZZLE
import ru.dpav.core.model.WeatherType.FOG
import ru.dpav.core.model.WeatherType.OVERCAST
import ru.dpav.core.model.WeatherType.PARTLY_CLOUDY
import ru.dpav.core.model.WeatherType.RAIN
import ru.dpav.core.model.WeatherType.SAND
import ru.dpav.core.model.WeatherType.SNOW
import ru.dpav.core.model.WeatherType.THUNDERSTORM
import ru.dpav.core.model.WeatherType.TORNADO
import ru.dpav.weather.R

internal object WeatherIconAssociator {
    @DrawableRes
    fun getIconByWeatherType(weatherType: WeatherType, isNight: Boolean) = when {
        weatherType == CLEAR && !isNight -> R.drawable.weather_icon_01
        weatherType == CLEAR -> R.drawable.weather_icon_01n
        weatherType == PARTLY_CLOUDY && !isNight -> R.drawable.weather_icon_02
        weatherType == PARTLY_CLOUDY -> R.drawable.weather_icon_02n
        weatherType == CLOUDY -> R.drawable.weather_icon_03
        weatherType == OVERCAST -> R.drawable.weather_icon_04
        weatherType == RAIN -> R.drawable.weather_icon_09
        weatherType == DRIZZLE && !isNight -> R.drawable.weather_icon_10
        weatherType == DRIZZLE -> R.drawable.weather_icon_10n
        weatherType == THUNDERSTORM -> R.drawable.weather_icon_11
        weatherType == SNOW -> R.drawable.weather_icon_13
        weatherType == FOG || weatherType == TORNADO || weatherType == SAND -> R.drawable.weather_icon_50
        else -> R.drawable.weather_icon_01
    }
}
