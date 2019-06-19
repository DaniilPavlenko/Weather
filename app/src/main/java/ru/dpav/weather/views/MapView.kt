package ru.dpav.weather.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.dpav.weather.api.City

@StateStrategyType(OneExecutionStateStrategy::class)
interface MapView : MvpView {
	@StateStrategyType(AddToEndSingleStrategy::class)
	fun setMapMarker(latLng: LatLng)

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun updateMapMarkers(cities: List<City>)

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun openInfoWindow(marker: Marker)

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun closeInfoWindow()

	fun showDetailInfo(city: City)

	fun moveCameraTo(latLng: LatLng, zoom: Float?)

	fun moveToStartPosition()

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun enableLocation(isEnable: Boolean)

	@StateStrategyType(AddToEndSingleStrategy::class)
	fun showUpdateScreen(isShow: Boolean)

	fun showConnectionError(latLng: LatLng)

	fun showToastError(message: String)

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun showLocationIsDisabled()

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun askPermission()
}