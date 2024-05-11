package ru.dpav.weather.data.api.model

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String,
    @SerializedName("coord")
    val coordinates: Coordinates,
    @SerializedName("main")
    val main: MainWeatherInfo,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("clouds")
    val clouds: Clouds,
    @SerializedName("weather")
    val weather: List<Weather>,
)
