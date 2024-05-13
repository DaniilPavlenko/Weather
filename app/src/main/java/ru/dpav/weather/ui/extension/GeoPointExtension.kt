package ru.dpav.weather.ui.extension

import org.osmdroid.util.GeoPoint
import ru.dpav.weather.domain.model.GeoCoordinate

internal fun GeoPoint.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = longitude)
