package ru.dpav.weather.core.data

import ru.dpav.weather.core.model.CityWeather

interface WeatherRepository {
    val citiesWeather: List<CityWeather>

    suspend fun fetchWeatherAt(latitude: Double, longitude: Double): Result<List<CityWeather>>
}
