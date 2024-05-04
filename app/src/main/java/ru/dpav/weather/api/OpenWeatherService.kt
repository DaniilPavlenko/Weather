package ru.dpav.weather.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.dpav.weather.api.model.WeatherResponse

interface OpenWeatherService {
    @GET("find/?cluster=yes&units=metric&cnt=$CITIES_COUNT&lang=$LANGUAGE")
    fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
    ): Single<WeatherResponse>

    companion object {
        const val CITIES_COUNT = 50
        const val LANGUAGE = "es"
    }
}
