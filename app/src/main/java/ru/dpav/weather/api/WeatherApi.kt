package ru.dpav.weather.api

import org.osmdroid.util.GeoPoint
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherApi {
	private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
	private val retrofit: Retrofit

	init {
		retrofit = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
	}

	fun getWeatherByCoordinates(
		point: GeoPoint,
		callback: Callback<WeatherResponse>) {
		val openWeatherService = retrofit.create(OpenWeatherService::class.java)
		val call = openWeatherService.getWeather(point.latitude, point.longitude)
		call.enqueue(callback)
	}
}