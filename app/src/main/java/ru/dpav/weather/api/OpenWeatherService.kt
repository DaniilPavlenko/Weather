package ru.dpav.weather.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
	@GET("find/?cnt=20&lang=ru&cluster=yes&units=metric&appid=$OW_API_KEY")
	fun getWeather(
		@Query("lat") latitude: Double,
		@Query("lon") longitude: Double): Single<WeatherResponse>

	companion object {
		const val OW_API_KEY: String = "58e49723652bbfd84d7021b8f146c685"
	}
}