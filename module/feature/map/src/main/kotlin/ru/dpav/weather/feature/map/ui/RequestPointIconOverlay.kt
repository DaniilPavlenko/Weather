package ru.dpav.weather.feature.map.ui

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import org.osmdroid.api.IGeoPoint
import org.osmdroid.views.overlay.IconOverlay
import ru.dpav.weather.feature.map.R

private const val VERTICAL_ANCHOR_POINT_OF_TOUCH_MARKER = 0.912F

internal class RequestPointIconOverlay(resources: Resources) : IconOverlay() {

    private val userLocationDrawable =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_marker_location, null)!!
    private val touchedPointDrawable =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_marker_search_final, null)!!

    var isUserLocation: Boolean = false
        set(value) {
            field = value
            mAnchorV = if (value) ANCHOR_CENTER else VERTICAL_ANCHOR_POINT_OF_TOUCH_MARKER
            mIcon = if (value) userLocationDrawable else touchedPointDrawable
        }

    fun setPosition(point: IGeoPoint) {
        mPosition = point
    }
}
