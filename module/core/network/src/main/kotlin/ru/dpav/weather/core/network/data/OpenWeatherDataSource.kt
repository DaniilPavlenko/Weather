package ru.dpav.weather.core.network.data

import retrofit2.HttpException
import ru.dpav.weather.core.model.CityWeather
import ru.dpav.weather.core.network.WeatherDataSource
import ru.dpav.weather.core.network.data.api.OpenWeatherService
import ru.dpav.weather.core.network.data.extension.toCityWeather
import ru.dpav.weather.core.network.data.model.City
import javax.inject.Inject

internal class OpenWeatherDataSource @Inject constructor(
    private val api: OpenWeatherService,
) : WeatherDataSource {

    override suspend fun fetchWeatherAt(
        latitude: Double,
        longitude: Double,
    ): Result<List<CityWeather>> {
        return runCatching { api.getWeather(latitude, longitude) }
            .recoverCatching { throwable: Throwable ->
                if (throwable is HttpException && throwable.code() == 404) {
                    return Result.success(emptyList())
                }
                throw Exception(throwable)
            }
            .map { response -> response.cities.map(City::toCityWeather) }
    }
}
