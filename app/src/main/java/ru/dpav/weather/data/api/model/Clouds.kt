package ru.dpav.weather.data.api.model

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all")
    val cloudy: Int,
)
