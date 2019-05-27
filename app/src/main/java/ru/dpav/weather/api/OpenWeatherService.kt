package ru.dpav.weather.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    @GET("find/?cnt=20&lang=ru&cluster=yes&units=metric")
    fun getWeather(@Query("lat") latitude: Double, @Query("lon") longitude: Double,
        @Query("appid") appId: String): Call<WeatherResponse>
}