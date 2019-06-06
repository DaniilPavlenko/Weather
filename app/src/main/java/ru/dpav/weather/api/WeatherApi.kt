package ru.dpav.weather.api

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.dpav.weather.R

class WeatherApi {
	private val retrofit: Retrofit
	private var unfinishedResponse: Response<WeatherResponse>? = null
	private var listener: ResponseListener? = null

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

	fun setUnfinishedResponse(response: Response<WeatherResponse>) {
		if (listener != null) {
			listener?.onUnfinishedResponse(response)
		} else {
			unfinishedResponse = response
		}
	}

	fun attachListener(responseListener: ResponseListener) {
		listener = responseListener
		unfinishedResponse?.let {
			listener?.onUnfinishedResponse(it)
		}
	}

	fun detachListener() {
		listener = null
	}

	interface ResponseListener {
		fun onUnfinishedResponse(response: Response<WeatherResponse>)
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