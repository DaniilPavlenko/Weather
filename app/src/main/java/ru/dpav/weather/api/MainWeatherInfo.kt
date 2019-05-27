package ru.dpav.weather.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MainWeatherInfo {
    @SerializedName("temp")
    @Expose
    internal val temp: Float = 0f

    @SerializedName("pressure")
    @Expose
    internal val pressure: Float = 0f
}