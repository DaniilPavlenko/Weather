package ru.dpav.weather.presenters

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpFacade
import com.arellomobile.mvp.MvpPresenter
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dpav.weather.R
import ru.dpav.weather.api.City
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.WeatherResponse
import ru.dpav.weather.views.MapView
import java.io.IOException

@InjectViewState
class MapPresenter : MvpPresenter<MapView>() {
	private var mCities: List<City> = ArrayList()
	private var isFirstCreate = true
	private lateinit var mContext: Context

	fun setContext(context: Context) {
		mContext = context
	}

	fun onSetMarker(latLng: LatLng) {
		viewState.setMapMarker(latLng)
		getWeather(latLng)
	}

	fun onMapClick(latLng: LatLng) {
		viewState.setMapMarker(latLng)
		viewState.showUpdateScreen()
		viewState.disableLocation()
		getWeather(latLng)
	}

	fun onCameraMoveTo(latLng: LatLng, zoom: Float) {
		viewState.moveCameraTo(latLng, zoom)
	}

	private fun getWeather(latLng: LatLng) {
		WeatherApi.getInstance().getWeatherByCoordinates(mContext, latLng,
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
		if (mCities == cities) {
			viewState.hideUpdateScreen()
			return
		}
		mCities = cities
		viewState.updateMapMarkers(cities)
		viewState.hideUpdateScreen()
		(MvpFacade.getInstance().presenterStore.get(ListPresenter.TAG_PRESENTER) as ListPresenter).onCitiesUpdate(cities)
	}

	fun onMarkerClick(marker: Marker) {
		viewState.openInfoWindow(marker)
	}

	fun onInfoWindowClick(marker: Marker) {
		for (i in 0 until mCities.size) {
			if (mCities[i].name == marker.title) {
				viewState.showDetailInfo(mCities[i])
			}
		}
	}

	fun onLocationEnable() {
		viewState.showUpdateScreen()
		viewState.enableLocation()
	}

	fun onLocationDisable() {
		viewState.disableLocation()
	}

	fun onLocationIsDisabled() {
		viewState.showLocationIsDisabled()
	}

	fun onServicesAvailable() {}

	fun onServicesUnavailable() {
		viewState.hideUpdateScreen()
	}

	fun onMapReady() {
		if (isFirstCreate) {
			isFirstCreate = false
			viewState.moveToStartPosition()
		}
	}

	fun onMoveToDefaultPosition() {
		val defaultLatLng = LatLng(
			mContext.getString(R.string.defaultLatitude).toDouble(),
			mContext.getString(R.string.defaultLongitude).toDouble())
		viewState.moveToDefaultPosition(defaultLatLng)
	}

	fun onInfoWindowClose() {
		viewState.closeInfoWindow()
	}

	fun onAskPermission() {
		if (isFirstCreate) {
			viewState.askPermission()
		}
	}
}