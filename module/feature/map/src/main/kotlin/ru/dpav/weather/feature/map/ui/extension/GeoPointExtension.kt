package ru.dpav.weather.feature.map.ui.extension

import org.osmdroid.util.GeoPoint
import ru.dpav.weather.core.model.GeoCoordinate

internal fun GeoPoint.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = longitude)
