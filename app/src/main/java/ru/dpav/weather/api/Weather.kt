package ru.dpav.weather.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Weather {
    @SerializedName("id")
    @Expose
    internal val id: Int? = null

    @SerializedName("icon")
    @Expose
    internal val icon: String = "01d"
}