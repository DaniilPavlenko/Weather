package ru.dpav.weather.data

import ru.dpav.core.model.CityWeather
import ru.dpav.weather.core.network.WeatherDataSource
import javax.inject.Inject
import javax.inject.Singleton

// TODO: When WeatherRepository will extend an interface. Remove @Singleton annotation.
@Singleton
class WeatherRepository @Inject constructor(
    private val dataSource: WeatherDataSource
) {

    var citiesWeather: List<CityWeather> = emptyList()
        private set

    suspend fun fetchWeatherAt(latitude: Double, longitude: Double): Result<List<CityWeather>> {
        return dataSource.fetchWeatherAt(latitude = latitude, longitude = longitude)
            .onSuccess { citiesWeather = it }
    }

}
