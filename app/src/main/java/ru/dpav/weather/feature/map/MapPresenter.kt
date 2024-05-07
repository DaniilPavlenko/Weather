package ru.dpav.weather.feature.map

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.data.WeatherRepository
import ru.dpav.weather.feature.map.MapDefaults.DEFAULT_POINT
import ru.dpav.weather.feature.map.MapDefaults.DEFAULT_ZOOM
import java.io.IOException

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var mErrorConnectionPoint: GeoPoint? = null
    private var getWeatherJob: Job? = null

    private val weatherRepository = WeatherRepository

    override fun onDestroy() {
        getWeatherJob?.cancel()
        presenterScope.cancel()
        super.onDestroy()
    }

    fun onSetMarker(point: GeoPoint) {
        viewState.setMapMarker(point)
        getWeather(point)
    }

    fun onMapClick(point: GeoPoint) {
        with(viewState) {
            enableLocation(false)
            setMapMarker(point)
            showUpdateScreen(true)
        }
        getWeather(point)
    }

    fun onRetryConnection() {
        mErrorConnectionPoint?.let {
            onMapClick(it)
        }
    }

    fun onSetCurrentPosition(
        point: GeoPoint,
        zoom: Double,
    ) {
        viewState.setCurrentPosition(point, zoom)
    }

    fun onCameraMoveTo(
        point: GeoPoint,
        zoom: Double,
    ) {
        viewState.moveCameraTo(point, zoom)
    }

    private fun getWeather(point: GeoPoint) {
        getWeatherJob?.cancel()
        getWeatherJob = presenterScope.launch {
            val weatherResult = weatherRepository.fetchWeatherAt(point.latitude, point.longitude)
            weatherResult
                .onSuccess(::handleCitiesUpdate)
                .onFailure { throwable ->
                    when (throwable) {
                        is IOException -> {
                            mErrorConnectionPoint = point
                            viewState.showConnectionError(true)
                        }
                        is HttpException -> {
                            handleCitiesUpdate(null)
                        }
                        else -> {
                            viewState.showSnack(R.string.error_unexpected)
                            viewState.showUpdateScreen(false)
                        }
                    }
                }
        }
    }

    private fun handleCitiesUpdate(cities: List<City>?) {
        with(viewState) {
            updateCitiesMarkers(cities)
            showConnectionError(false)
            showUpdateScreen(false)
        }
    }

    fun onMarkerClick(markerId: String) {
        viewState.openInfoWindow(markerId)
    }

    fun onLocationEnable() {
        with(viewState) {
            showUpdateScreen(true)
            enableLocation(true)
        }
    }

    fun onLocationDisable() {
        viewState.enableLocation(false)
    }

    fun onLocationIsDisabled() {
        with(viewState) {
            showUpdateScreen(false)
            enableLocation(false)
            showSnack(R.string.geo_disabled)
        }
    }

    fun onServicesUnavailable() {
        viewState.showUpdateScreen(false)
    }

    fun onMoveToDefaultPosition() {
        onMapClick(DEFAULT_POINT)
        onCameraMoveTo(DEFAULT_POINT, DEFAULT_ZOOM)
    }

    fun onInfoWindowClose() {
        viewState.closeInfoWindow()
    }

    override fun onFirstViewAttach() {
        with(viewState) {
            askPermission()
            setStartPosition()
        }
    }
}
