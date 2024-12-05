package ru.dpav.weather.core.network.data.model

import com.google.gson.annotations.SerializedName

internal data class Wind(
    @SerializedName("speed")
    val speed: Float,
    @SerializedName("deg")
    val deg: Float,
)
