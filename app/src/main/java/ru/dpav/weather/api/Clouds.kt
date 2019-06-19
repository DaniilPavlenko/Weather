package ru.dpav.weather.api

import com.google.gson.annotations.SerializedName

data class Clouds(
	@SerializedName("all") val cloudy: Int
)