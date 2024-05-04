package ru.dpav.weather.api.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val message: String,
    @SerializedName("list") val cities: List<City>,
)
