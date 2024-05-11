package ru.dpav.weather.data

import ru.dpav.weather.data.api.WeatherApi
import ru.dpav.weather.data.api.model.City
import ru.dpav.weather.data.api.model.WeatherResponse

object WeatherRepository {

    private val api = WeatherApi.api

    var cities: List<City> = emptyList(); private set

    suspend fun fetchWeatherAt(latitude: Double, longitude: Double): Result<List<City>> {
        return runCatching { api.getWeather(latitude, longitude) }
            .map(WeatherResponse::cities)
            .onSuccess { cities = it }
    }

}
