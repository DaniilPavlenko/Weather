package ru.dpav.weather.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WeatherResponse {
	@SerializedName("message")
	@Expose
	internal var message: String? = null

	@SerializedName("list")
	@Expose
	internal var cities: List<City>? = null
}