package ru.dpav.weather.core.network.data.model

import com.google.gson.annotations.SerializedName

internal data class Clouds(
    @SerializedName("all")
    val cloudy: Int,
)
