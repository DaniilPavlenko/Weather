package ru.dpav.weather.api

import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object WeatherApi {
	private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
	private val retrofit: Retrofit
	val api: OpenWeatherService

	init {
		retrofit = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(
				RxJava2CallAdapterFactory
					.createWithScheduler(Schedulers.io())
			)
			.build()
		api = retrofit.create(OpenWeatherService::class.java)
	}
}