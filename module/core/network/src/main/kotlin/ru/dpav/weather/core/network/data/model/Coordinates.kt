package ru.dpav.weather.core.network.data.model

import com.google.gson.annotations.SerializedName

internal data class Coordinates(
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
)
