package ru.dpav.weather.api

import com.google.gson.annotations.SerializedName

data class City(
	val id: Int,
	val name: String,
	@SerializedName("coord") val coordinates: Coordinates,
	val main: MainWeatherInfo,
	val wind: Wind,
	val clouds: Clouds,
	val weather: List<Weather>
)