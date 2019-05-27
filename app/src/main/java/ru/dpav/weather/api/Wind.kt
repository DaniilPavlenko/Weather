package ru.dpav.weather.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Wind {
    @SerializedName("speed")
    @Expose
    internal val speed: Float = 0f
}