package ru.dpav.weather.core.network

import ru.dpav.weather.core.model.CityWeather

interface WeatherDataSource {
    suspend fun fetchWeatherAt(latitude: Double, longitude: Double): Result<List<CityWeather>>
}
