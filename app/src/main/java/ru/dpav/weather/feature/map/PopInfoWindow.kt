package ru.dpav.weather.feature.map

import android.view.View
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.databinding.InfoWindowWeatherBinding
import ru.dpav.weather.ui.WeatherIconAssociator

open class PopInfoWindow(
    layoutResId: Int,
    mapView: MapView,
    private val city: City,
    private val onClick: View.OnClickListener,
) : InfoWindow(layoutResId, mapView) {

    override fun onOpen(item: Any?) {
        val binding = InfoWindowWeatherBinding.bind(mView)
        with(binding) {
            infoWindowTitle.text = city.name

            val icon = WeatherIconAssociator.getIconByName(city.weather[0].icon)
            infoWindowTitle.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

            infoWindowSnippet.text = root.context.getString(
                R.string.marker_snippet,
                city.main.temp.toInt(),
                city.wind.speed.toInt(),
                city.clouds.cloudy,
                city.main.pressureInMmHg
            )

            root.setOnClickListener(onClick)
        }
    }

    override fun onClose() {}
}
