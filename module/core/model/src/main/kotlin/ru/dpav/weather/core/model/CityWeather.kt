package ru.dpav.weather.core.model

data class CityWeather(
    val cityId: Int,
    val cityName: String,
    val coordinate: GeoCoordinate,
    val weatherType: WeatherType,
    val isNight: Boolean,
    val temperature: Int,
    val windSpeed: Speed,
    val cloudiness: Percent,
    val humidity: Percent,
    val pressure: Pressure,
)
