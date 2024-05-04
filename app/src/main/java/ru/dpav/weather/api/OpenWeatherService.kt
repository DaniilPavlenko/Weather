package ru.dpav.weather.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.dpav.weather.BuildConfig

interface OpenWeatherService {
    @GET("find/?cluster=yes&units=metric&cnt=$CITIES_COUNT&lang=$LANGUAGE&appid=$API_KEY")
    fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
    ): Single<WeatherResponse>

    companion object {
        const val API_KEY = BuildConfig.OPEN_WEATHER_API_KEY
        const val CITIES_COUNT = 50
        const val LANGUAGE = "es"
    }
}
