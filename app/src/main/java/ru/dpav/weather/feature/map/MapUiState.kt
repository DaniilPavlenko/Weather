package ru.dpav.weather.feature.map

import ru.dpav.weather.data.api.model.City
import ru.dpav.weather.domain.model.GeoCoordinate

data class MapUiState(
    val isLoading: Boolean = true,
    val requestedPosition: GeoCoordinate? = null,
    val cities: List<City> = emptyList(),
    val hasConnectionError: Boolean = false,
    val showTurnedOffGeoInfo: Boolean = false,
    val isLocationServicesAvailable: Boolean = false,
    val isLocationUpdating: Boolean = false,
)
