package ru.dpav.weather.feature.map

import android.view.View
import kotlinx.android.synthetic.main.info_window_weather.view.infoWindowButtonsContainer
import kotlinx.android.synthetic.main.info_window_weather.view.infoWindowEditButton
import kotlinx.android.synthetic.main.info_window_weather.view.infoWindowRemoveButton
import org.osmdroid.views.MapView
import ru.dpav.weather.api.model.City

class EditablePopInfo(
    layoutResId: Int,
    mapView: MapView?,
    city: City,
    private val listener: WindowInfoListener,
) : PopInfoWindow(
    layoutResId,
    mapView,
    city,
    View.OnClickListener { listener.onWindowClick() }
) {

    override fun onOpen(item: Any?) {
        super.onOpen(item)
        mView.infoWindowButtonsContainer.visibility = View.VISIBLE
        mView.infoWindowEditButton.setOnClickListener {
            listener.onEditClick()
        }
        mView.infoWindowRemoveButton.setOnClickListener {
            listener.onRemoveClick()
        }
    }

    interface WindowInfoListener {
        fun onWindowClick()
        fun onEditClick()
        fun onRemoveClick()
    }
}
