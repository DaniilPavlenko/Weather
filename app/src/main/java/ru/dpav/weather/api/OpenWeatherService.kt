package ru.dpav.weather.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    @GET(
        "find/?cluster=yes&units=metric" +
            "&cnt=$CITIES_COUNT&lang=$LANGUAGE&appid=$OW_API_KEY"
    )
    fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
    ): Single<WeatherResponse>

    companion object {
        const val OW_API_KEY = "58e49723652bbfd84d7021b8f146c685"
        const val CITIES_COUNT = 50
        const val LANGUAGE = "es"
    }
}
