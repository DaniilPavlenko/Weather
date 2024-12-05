package ru.dpav.weather.feature.map

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.dpav.weather.data.WeatherRepository
import ru.dpav.weather.domain.model.GeoCoordinate
import javax.inject.Inject

private const val TAG = "MapViewModel"
private const val SAVED_MAP_CENTER = "map_center"
private const val SAVED_MAP_ZOOM_LEVEL = "map_zoom"

@HiltViewModel
class MapViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var _uiState = MutableStateFlow(MapUiState())
    val uiState get() = _uiState.asStateFlow()

    val mapCenter = savedStateHandle.getStateFlow(SAVED_MAP_CENTER, MapDefaults.MAP_CENTER)
    val mapZoomLevel = savedStateHandle.getStateFlow(SAVED_MAP_ZOOM_LEVEL, MapDefaults.ZOOM)

    private var currentWeatherListCoordinate: GeoCoordinate? = null
    private var failedRequestCoordinate: GeoCoordinate? = null

    private var getWeatherJob: Job? = null

    fun onRequestWeatherAt(coordinate: GeoCoordinate) {
        if (coordinate == currentWeatherListCoordinate) {
            return
        }
        getWeather(coordinate)
    }

    fun onRetryRequest() {
        failedRequestCoordinate?.let(::onRequestWeatherAt)
    }

    private fun getWeather(coordinate: GeoCoordinate) {
        getWeatherJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = true,
                hasConnectionError = false,
                requestedPosition = coordinate
            )
        }
        getWeatherJob = viewModelScope.launch {
            failedRequestCoordinate = null

            weatherRepository.fetchWeatherAt(coordinate.latitude, coordinate.longitude)
                .onSuccess { citiesWeather ->
                    currentWeatherListCoordinate = coordinate
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            citiesWeather = citiesWeather,
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "getWeather: on failure", throwable)
                    failedRequestCoordinate = coordinate
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasConnectionError = true
                        )
                    }
                }
        }
    }

    fun onLocationServiceUnavailable() {
        _uiState.update { it.copy(isLocationServicesAvailable = false) }
    }

    fun onLocationServiceAvailable() {
        _uiState.update { it.copy(isLocationServicesAvailable = true) }
    }

    fun saveMapState(mapCenter: DoubleArray, zoomLevel: Double) {
        savedStateHandle[SAVED_MAP_CENTER] = mapCenter
        savedStateHandle[SAVED_MAP_ZOOM_LEVEL] = zoomLevel
    }

    fun onLocationTurnedOff() {
        _uiState.update { it.copy(showTurnedOffGeoInfo = true) }
    }

    fun onShownDisabledGeoInfo() {
        _uiState.update { it.copy(showTurnedOffGeoInfo = false) }
    }

    fun onLocationUpdateRequested() {
        _uiState.update { it.copy(isLocationUpdating = true) }
    }

    fun onLocationUpdateCompleted() {
        _uiState.update { it.copy(isLocationUpdating = false) }
    }

    fun onLocationUpdateCancel() {
        _uiState.update { it.copy(isLocationUpdating = false) }
    }
}
