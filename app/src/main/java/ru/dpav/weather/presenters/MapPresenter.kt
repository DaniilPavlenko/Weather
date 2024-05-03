package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import retrofit2.HttpException
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.Constants
import ru.dpav.weather.R
import ru.dpav.weather.api.City
import ru.dpav.weather.api.Coordinates
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.WeatherResponse
import ru.dpav.weather.views.MapView
import java.io.IOException

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {

    private var mErrorConnectionPoint: GeoPoint? = null
    private var mDisposableRequest: Disposable? = null
    private var mEditingMarkerId: String? = null
    private var mCityForRemove: City? = null

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

    fun saveCity() {
        mEditingMarkerId?.let {
            with(viewState) {
                closeInfoWindow()
                addCustomCity(
                    CitiesRepository.customCities.first { city ->
                        city.id.toString() == mEditingMarkerId
                    }
                )
                openInfoWindow(it)
            }
            mEditingMarkerId = null
            return
        }
        viewState.addCustomCity(CitiesRepository.customCities.last())
    }

    fun setEditingMarkerId(markerId: String) {
        mEditingMarkerId = markerId
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
        onMapClick(Constants.DEFAULT_POINT)
        onCameraMoveTo(
            Constants.DEFAULT_POINT,
            Constants.DEFAULT_ZOOM
        )
    }

    fun onRemoveClick(city: City) {
        mCityForRemove = city
        viewState.showRemoveDialog(true)
    }

    fun onAcceptDialog() {
        mCityForRemove?.let {
            CitiesRepository.removeCustomCity(it)
        }
        mCityForRemove = null
        viewState.showRemoveDialog(false)
    }

    fun onDeclineDialog() {
        mCityForRemove = null
        viewState.showRemoveDialog(false)
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

    fun onCustomCityDragEnd(marker: Marker) {
        CitiesRepository.customCities.forEachIndexed { index, city ->
            if (city.id.toString() == marker.id) {
                CitiesRepository.customCities[index].coordinates =
                    Coordinates(
                        marker.position.latitude,
                        marker.position.longitude
                    )
            }
        }
    }

    fun onCustomCityRemoved() {
        with(viewState) {
            closeInfoWindow()
            updateCitiesMarkers()
        }
        mEditingMarkerId = null
    }
}
