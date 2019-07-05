package ru.dpav.weather.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import org.osmdroid.util.GeoPoint
import ru.dpav.weather.api.City

@StateStrategyType(AddToEndSingleStrategy::class)
interface MapView : MvpView {
	fun setMapMarker(point: GeoPoint)
	fun addCustomCity(city: City)
	fun updateCitiesMarkers()
	fun openInfoWindow(cityId: String)
	fun closeInfoWindow()
	fun setCurrentPosition(point: GeoPoint, zoom: Double)
	fun enableLocation(enable: Boolean)
	fun showUpdateScreen(shown: Boolean)
	fun showConnectionError(shown: Boolean)
	fun showRemoveDialog(shown: Boolean)

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun moveCameraTo(point: GeoPoint, zoom: Double?)

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun setStartPosition()

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun showSnack(resId: Int)

	@StateStrategyType(OneExecutionStateStrategy::class)
	fun askPermission()
}