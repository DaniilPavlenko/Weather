package ru.dpav.weather.core.data.impl

import ru.dpav.core.model.CityWeather
import ru.dpav.weather.core.data.WeatherRepository
import ru.dpav.weather.core.network.WeatherDataSource
import javax.inject.Inject

internal class WeatherRepositoryImpl @Inject constructor(
    private val dataSource: WeatherDataSource,
) : WeatherRepository {

    override var citiesWeather: List<CityWeather> = emptyList()
        private set

    override suspend fun fetchWeatherAt(
        latitude: Double,
        longitude: Double,
    ): Result<List<CityWeather>> {
        return dataSource.fetchWeatherAt(latitude = latitude, longitude = longitude)
            .onSuccess { citiesWeather = it }
    }

}
