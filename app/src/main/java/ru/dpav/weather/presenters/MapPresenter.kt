package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.osmdroid.util.GeoPoint
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.Constants
import ru.dpav.weather.R
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.WeatherResponse
import ru.dpav.weather.views.MapView
import java.io.IOException

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {
	private var mErrorConnectionPoint: GeoPoint? = null
	private var disposableRequest: Disposable? = null

	override fun onDestroy() {
		disposableRequest?.dispose()
		super.onDestroy()
	}

	fun onSetMarker(point: GeoPoint) {
		viewState.setMapMarker(point)
		getWeather(point)
	}

	fun onMapClick(point: GeoPoint) {
		with(viewState) {
			setMapMarker(point)
			showUpdateScreen(true)
			enableLocation(false)
		}
		getWeather(point)
	}

	fun onRetryConnection() {
		mErrorConnectionPoint?.let {
			onMapClick(it)
		}
	}

	fun onSetCurrentPosition(point: GeoPoint, zoom: Double) {
		viewState.setCurrentPosition(point, zoom)
	}

	fun onCameraMoveTo(point: GeoPoint, zoom: Double) {
		viewState.moveCameraTo(point, zoom)
	}

	private fun getWeather(point: GeoPoint) {
		disposableRequest = WeatherApi.api
			.getWeather(point.latitude, point.longitude)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({
				processApiResponse(it)
				viewState.showConnectionError(false)
			}, {
				if (it is IOException) {
					mErrorConnectionPoint = point
					viewState.showConnectionError(true)
				}
			})
	}

	private fun processApiResponse(response: WeatherResponse) {
		val cities = response.cities
		CitiesRepository.cities = cities
		with(viewState) {
			updateCitiesMarkers(cities)
			showUpdateScreen(false)
		}
	}

	fun onMarkerClick(position: Int) {
		viewState.openInfoWindow(position)
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
			Constants.DEFAULT_ZOOM)
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