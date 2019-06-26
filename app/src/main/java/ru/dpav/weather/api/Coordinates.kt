package ru.dpav.weather.api

import com.google.gson.annotations.SerializedName

data class Coordinates(
	@SerializedName("lat") var latitude: Double,
	@SerializedName("lon") var longitude: Double
)