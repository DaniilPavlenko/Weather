package ru.dpav.weather

import android.view.View
import kotlinx.android.synthetic.main.info_window_weather.view.*
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import ru.dpav.weather.api.City
import ru.dpav.weather.util.Util

class PopInfoWindow(
	layoutResId: Int,
	mapView: MapView?,
	private val city: City,
	private val onClick: View.OnClickListener
) : InfoWindow(layoutResId, mapView) {
	override fun onOpen(item: Any?) {
		mView.infoWindowTitle.text = city.name

		val icon = Util.getWeatherIconByName(city.weather[0].icon)
		mView.infoWindowTitle
			.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

		mView.infoWindowSnippet.text = mView.context.getString(
			R.string.marker_snippet,
			city.main.temp.toInt(),
			city.wind.speed.toInt(),
			city.clouds.cloudy,
			Util.getPressureInMmHg(city.main.pressure))

		mView.setOnClickListener(onClick)
	}

	override fun onClose() {}
}