package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.Constants
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.WeatherResponse
import ru.dpav.weather.views.MapView
import java.io.IOException

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {
	fun onSetMarker(latLng: LatLng) {
		viewState.setMapMarker(latLng)
		getWeather(latLng)
	}

	fun onMapClick(latLng: LatLng) {
		viewState.setMapMarker(latLng)
		viewState.showUpdateScreen(true)
		viewState.enableLocation(false)
		getWeather(latLng)
	}

	fun onCameraMoveTo(latLng: LatLng, zoom: Float) {
		viewState.moveCameraTo(latLng, zoom)
	}

	private fun getWeather(latLng: LatLng) {
		WeatherApi.getInstance().getWeatherByCoordinates(
			latLng,
			object : Callback<WeatherResponse> {
				override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
					if (t is IOException) {
						viewState.showConnectionError(latLng)
					}
				}

				override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
					processApiResponse(response)
				}
			})
	}

	fun processApiResponse(response: Response<WeatherResponse>) {
		val cities = response.body()?.cities
		if (cities == null) {
			response.body()?.message?.let { viewState.showToastError(it) }
			return
		}
		if (CitiesRepository.cities == cities) {
			viewState.showUpdateScreen(false)
			return
		}
		CitiesRepository.cities = cities
		viewState.updateMapMarkers(cities)
		viewState.showUpdateScreen(false)
	}

	fun onMarkerClick(marker: Marker) {
		viewState.openInfoWindow(marker)
	}

	fun onInfoWindowClick(marker: Marker) {
		CitiesRepository.cities.forEach {
			if (it.name == marker.title) {
				viewState.showDetailInfo(it)
				return@forEach
			}
		}
	}

	fun onLocationEnable() {
		viewState.showUpdateScreen(true)
		viewState.enableLocation(true)
	}

	fun onLocationDisable() {
		viewState.enableLocation(false)
	}

	fun onLocationIsDisabled() {
		viewState.showUpdateScreen(false)
		viewState.enableLocation(false)
		viewState.showLocationIsDisabled()
	}

	fun onServicesAvailable() {}

	fun onServicesUnavailable() {
		viewState.showUpdateScreen(false)
	}

	fun onMoveToDefaultPosition() {
		val latLng = LatLng(
			Constants.DEFAULT_LATITUDE,
			Constants.DEFAULT_LONGITUDE)
		onMapClick(latLng)
		onCameraMoveTo(latLng, Constants.DEFAULT_ZOOM)
	}

	fun onInfoWindowClose() {
		viewState.closeInfoWindow()
	}

	override fun onFirstViewAttach() {
		viewState.askPermission()
		viewState.moveToStartPosition()
	}
}