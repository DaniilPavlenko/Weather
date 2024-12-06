package ru.dpav.weather.feature.map.ui

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import ru.dpav.weather.common.ui.WeatherIconAssociator
import ru.dpav.weather.core.model.CityWeather
import ru.dpav.weather.feature.map.R
import ru.dpav.weather.feature.map.databinding.InfoWindowWeatherBinding

internal class CityWeatherInfoWindow(
    mapView: MapView,
    private val cityWeather: CityWeather,
    private val onWindowClick: (cityWeather: CityWeather) -> Unit,
) : InfoWindow(R.layout.info_window_weather, mapView) {

    override fun onOpen(item: Any?) = with(InfoWindowWeatherBinding.bind(mView)) {
        root.setOnClickListener { onWindowClick(cityWeather) }

        infoWindowTitle.text = cityWeather.cityName
        val icon = WeatherIconAssociator.getIconByWeatherType(
            weatherType = cityWeather.weatherType,
            isNight = cityWeather.isNight
        )
        infoWindowTitle.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

        infoWindowSnippet.text = root.context.getString(
            R.string.marker_snippet,
            cityWeather.temperature,
            cityWeather.windSpeed.metersPerSecond,
            cityWeather.cloudiness.value,
            cityWeather.pressure.millimetersOfMercury
        )
    }

    override fun onClose() {}
}
