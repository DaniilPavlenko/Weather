package ru.dpav.weather.core.network.data.model

import com.google.gson.annotations.SerializedName

internal data class WeatherResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("list")
    val cities: List<City>,
)
