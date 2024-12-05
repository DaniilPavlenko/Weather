package ru.dpav.weather.ui.extension

import org.osmdroid.util.GeoPoint
import ru.dpav.core.model.GeoCoordinate

internal fun GeoPoint.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = longitude)
