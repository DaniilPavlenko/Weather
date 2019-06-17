package ru.dpav.weather.api

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.dpav.weather.R

class WeatherApi {
	private val retrofit: Retrofit

	init {
		retrofit = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
	}

	fun getWeatherByCoordinates(context: Context, latLng: LatLng, callback: Callback<WeatherResponse>) {
		val openWeatherService = retrofit.create(OpenWeatherService::class.java)
		val call = openWeatherService.getWeather(
			latLng.latitude, latLng.longitude,
			context.getString(R.string.open_weather_api_key)
		)
		call.enqueue(callback)
	}

	companion object {
		private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
		@Volatile
		private var instance: WeatherApi? = null

		fun getInstance(): WeatherApi {
			if (instance == null) {
				synchronized(this) {
					instance = WeatherApi()
				}
			}
			return instance!!
		}
	}
}