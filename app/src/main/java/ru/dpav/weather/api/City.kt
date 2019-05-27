package ru.dpav.weather.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class City {
    @SerializedName("id")
    @Expose
    internal val id: Int = 0

    @SerializedName("name")
    @Expose
    internal val name: String? = null

    @SerializedName("coord")
    @Expose
    internal val coordinates: Coordinates? = null

    @SerializedName("main")
    @Expose
    internal val main: MainWeatherInfo? = null

    @SerializedName("wind")
    @Expose
    internal val wind: Wind? = null

    @SerializedName("clouds")
    @Expose
    internal val clouds: Clouds? = null

    @SerializedName("weather")
    @Expose
    internal val weather: List<Weather>? = null

}