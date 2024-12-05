package ru.dpav.weather.core.network.data.model

import com.google.gson.annotations.SerializedName

data class MainWeatherInfo(
    @SerializedName("temp")
    val temp: Float,
    @SerializedName("pressure")
    val pressureInHpa: Float,
    @SerializedName("humidity")
    val humidity: Float,
)
