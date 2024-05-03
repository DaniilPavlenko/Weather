package ru.dpav.weather.feature.map

import android.view.View
import kotlinx.android.synthetic.main.info_window_weather.view.infoWindowSnippet
import kotlinx.android.synthetic.main.info_window_weather.view.infoWindowTitle
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import ru.dpav.weather.R
import ru.dpav.weather.api.City
import ru.dpav.weather.util.Util

open class PopInfoWindow(
    layoutResId: Int,
    mapView: MapView?,
    private val city: City,
    private val onClick: View.OnClickListener,
) : InfoWindow(layoutResId, mapView) {

    override fun onOpen(item: Any?) {
        mView.infoWindowTitle.text = city.name

        val icon = Util.Icons
            .getWeatherIconByName(city.weather[0].icon)

        mView.infoWindowTitle
            .setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

        mView.infoWindowSnippet.text = mView.context.getString(
            R.string.marker_snippet,
            city.main.temp.toInt(),
            city.wind.speed.toInt(),
            city.clouds.cloudy,
            Util.getPressureInMmHg(city.main.pressure)
        )

        mView.setOnClickListener(onClick)
    }

    override fun onClose() {}
}
