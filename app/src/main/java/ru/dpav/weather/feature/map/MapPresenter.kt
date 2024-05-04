package ru.dpav.weather.feature.map

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.R
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.model.WeatherResponse
import ru.dpav.weather.feature.map.MapDefaults.DEFAULT_POINT
import ru.dpav.weather.feature.map.MapDefaults.DEFAULT_ZOOM
import java.io.IOException

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    private var mErrorConnectionPoint: GeoPoint? = null
    private var mDisposableRequest: Disposable? = null

    override fun onDestroy() {
        mDisposableRequest?.dispose()
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
        mDisposableRequest = WeatherApi.api
            .getWeather(point.latitude, point.longitude)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                processApiResponse(it)
            }, {
                when (it) {
                    is IOException -> {
                        mErrorConnectionPoint = point
                        viewState.showConnectionError(true)
                    }
                    is HttpException -> {
                        processApiResponse(null)
                    }
                    else -> {
                        viewState.showSnack(R.string.error_unexpected)
                        viewState.showUpdateScreen(false)
                    }
                }
            })
    }

    private fun processApiResponse(response: WeatherResponse?) {
        CitiesRepository.cities = arrayListOf()
        response?.let {
            CitiesRepository.cities = it.cities
        }
        with(viewState) {
            updateCitiesMarkers()
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
