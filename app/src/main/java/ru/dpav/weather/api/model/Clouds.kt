package ru.dpav.weather.api.model

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all") var cloudy: Int,
)
