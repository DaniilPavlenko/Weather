package ru.dpav.weather.data

import io.reactivex.Single
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.model.City

object WeatherRepository {

    private val api = WeatherApi.api

    var cities: List<City> = emptyList(); private set

    fun fetchWeatherAt(latitude: Double, longitude: Double): Single<List<City>> {
        return api.getWeather(latitude, longitude)
            .map { weatherResponse -> weatherResponse.cities }
            .doOnSubscribe { cities = emptyList() }
            .doOnSuccess { cities -> this.cities = cities }
    }

}
