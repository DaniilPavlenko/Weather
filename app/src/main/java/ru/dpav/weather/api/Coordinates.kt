package ru.dpav.weather.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Coordinates {
    @SerializedName("lat")
    @Expose
    internal val latitude: Double = 0.0

    @SerializedName("lon")
    @Expose
    internal val longitude: Double = 0.0
}