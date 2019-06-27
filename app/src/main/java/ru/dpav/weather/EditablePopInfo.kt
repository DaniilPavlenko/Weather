package ru.dpav.weather

import android.view.View
import kotlinx.android.synthetic.main.info_window_weather.view.*
import org.osmdroid.views.MapView
import ru.dpav.weather.api.City

class EditablePopInfo(
	layoutResId: Int,
	mapView: MapView?,
	city: City,
	onClick: View.OnClickListener,
	private val onEditClick: View.OnClickListener)
	: PopInfoWindow(layoutResId, mapView, city, onClick) {

	override fun onOpen(item: Any?) {
		super.onOpen(item)
		mView.infoWindowEditButton.visibility = View.VISIBLE
		mView.infoWindowEditButton.setOnClickListener(onEditClick)
	}
}