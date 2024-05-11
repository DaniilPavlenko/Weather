package ru.dpav.weather.data.api.model

import com.google.gson.annotations.SerializedName

private const val HPA_TO_MM_HG_COEFFICIENT = 1.333f

data class MainWeatherInfo(
    @SerializedName("temp")
    val temp: Float,
    @SerializedName("pressure")
    val pressureInHpa: Float,
    @SerializedName("humidity")
    val humidity: Float,
) {
    // Can't use the Lazy delegate here because of GSON.
    // https://stackoverflow.com/q/55991838/6681665
    // https://programmerr47.medium.com/gson-unsafe-problem-d1ff29d4696f
    var pressureInMmHg: Int = 0
        private set
        get() {
            if (field == 0) {
                field = if (pressureInHpa > 850) {
                    (pressureInHpa / HPA_TO_MM_HG_COEFFICIENT).toInt()
                } else {
                    pressureInHpa.toInt()
                }
            }
            return field
        }
}
