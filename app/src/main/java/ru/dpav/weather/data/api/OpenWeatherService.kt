package ru.dpav.weather.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.dpav.weather.data.api.model.WeatherResponse

interface OpenWeatherService {
    @GET("find/")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("cluster") cluster: String = "yes",
        @Query("units") units: String = "metric",
        @Query("cnt") count: Int = MAX_CITIES_COUNT_PER_REQUEST,
        @Query("lang") language: String = LANGUAGE,
    ): WeatherResponse

    companion object {
        const val MAX_CITIES_COUNT_PER_REQUEST = 50
        const val LANGUAGE = "en"
    }
}
