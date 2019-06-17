package ru.dpav.weather.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.dpav.weather.api.City
import ru.dpav.weather.strategies.AddToEndSingleByTagStateStrategy

@StateStrategyType(OneExecutionStateStrategy::class)
interface MapView : MvpView {

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun setMapMarker(latLng: LatLng)

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun updateMapMarkers(cities: List<City>)

	@StateStrategyType(value = AddToEndSingleByTagStateStrategy::class, tag = TAG_INFO_WINDOW)
	fun openInfoWindow(marker: Marker)

	@StateStrategyType(value = AddToEndSingleByTagStateStrategy::class, tag = TAG_INFO_WINDOW)
	fun closeInfoWindow()

	fun showDetailInfo(city: City)

	fun moveCameraTo(latLng: LatLng, zoom: Float?)

	fun moveToStartPosition()

	fun moveToDefaultPosition(latLng: LatLng)

	@StateStrategyType(value = AddToEndSingleByTagStateStrategy::class, tag = TAG_LOCATION_LISTEN)
	fun enableLocation()

	@StateStrategyType(value = AddToEndSingleByTagStateStrategy::class, tag = TAG_LOCATION_LISTEN)
	fun disableLocation()

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun showUpdateScreen()

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun hideUpdateScreen()

	fun showConnectionError(latLng: LatLng)

	fun showToastError(message: String)

	fun showLocationIsDisabled()

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun askPermission()

	companion object {
		const val TAG_INFO_WINDOW: String = "tagInfoWindow"
		const val TAG_LOCATION_LISTEN: String = "tagLocationListen"
	}
}