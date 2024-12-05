package ru.dpav.weather.core.network.data.extension

import ru.dpav.core.model.CityWeather
import ru.dpav.core.model.GeoCoordinate
import ru.dpav.core.model.Percent
import ru.dpav.core.model.Pressure
import ru.dpav.core.model.Speed
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
import ru.dpav.core.model.exception.UnknownWeatherType
import ru.dpav.weather.core.network.data.model.City
import kotlin.math.roundToInt

internal fun City.toCityWeather(): CityWeather {
    return CityWeather(
        cityId = id,
        cityName = name,
        coordinate = GeoCoordinate(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        ),
        weatherType = getWeatherType(),
        isNight = weather.first().icon.endsWith('n'),
        temperature = main.temp.roundToInt(),
        windSpeed = Speed.metersPerSecond(wind.speed.roundToInt()),
        cloudiness = Percent(clouds.cloudy),
        humidity = Percent(main.humidity.roundToInt()),
        pressure = Pressure.hectopascals(main.pressureInHpa.roundToInt())
    )
}

/**
 * Parses more common [WeatherType] from [City.weather]
 *
 * [Status documentation](https://openweathermap.org/weather-conditions#Weather-Condition-Codes-2)
 */
private fun City.getWeatherType(): WeatherType {
    return when (weather.first().id) {
        in 200 until 299 -> THUNDERSTORM
        in 300 until 400 -> DRIZZLE
        in 500 until 600 -> RAIN
        in 600 until 700 -> SNOW
        701, 721, 741 -> FOG
        751 -> SAND
        781 -> TORNADO
        800 -> CLEAR
        801 -> PARTLY_CLOUDY
        802 -> CLOUDY
        803, 804 -> OVERCAST
        else -> throw UnknownWeatherType("Unhandled OpenWeather code: ${weather.first().id}")
    }
}
