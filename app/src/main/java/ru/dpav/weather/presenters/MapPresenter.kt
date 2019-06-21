package ru.dpav.weather.presenters

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
		WeatherApi.getWeatherByCoordinates(
			point,
			object : Callback<WeatherResponse> {
				override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
					if (t is IOException) {
						mErrorConnectionPoint = point
						viewState.showConnectionError(true)
					}
				}

				override fun onResponse(
					call: Call<WeatherResponse>,
					response: Response<WeatherResponse>) {
					processApiResponse(response)
					viewState.showConnectionError(false)
				}
			})
	}

	fun processApiResponse(response: Response<WeatherResponse>) {
		val cities = response.body()?.cities
		if (cities == null) {
			response.body()?.message?.let {
				viewState.showSnack(R.string.error_response)
				Log.e("ApiResponseError", it)
			}
			return
		}
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