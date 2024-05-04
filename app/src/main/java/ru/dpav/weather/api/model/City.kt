package ru.dpav.weather.api.model

import com.google.gson.annotations.SerializedName

class City {
    var id: Int = 0
    lateinit var name: String

    @SerializedName("coord")
    lateinit var coordinates: Coordinates
    lateinit var main: MainWeatherInfo
    lateinit var wind: Wind
    lateinit var clouds: Clouds
    lateinit var weather: List<Weather>

    companion object {
        fun getEmpty(): City {
            val city = City()
            city.id = 0
            city.name = ""
            city.coordinates = Coordinates(0e0, 0e0)
            city.wind = Wind(0f, 0f)
            city.weather = listOf(Weather(1, "01d"))
            city.main = MainWeatherInfo(0f, 0f, 0f)
            city.clouds = Clouds(0)
            return city
        }
    }
}
