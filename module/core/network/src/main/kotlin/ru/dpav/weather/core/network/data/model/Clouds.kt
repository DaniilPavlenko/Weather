package ru.dpav.weather.core.network.data.model

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all")
    val cloudy: Int,
)
