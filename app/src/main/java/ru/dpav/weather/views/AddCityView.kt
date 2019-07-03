package ru.dpav.weather.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.dpav.weather.api.City

@StateStrategyType(AddToEndSingleStrategy::class)
interface AddCityView : MvpView {
	fun setCity(city: City)
	fun save()
	fun cancel()
	fun setWeatherIcon(icon: Int)
	fun showNameError(message: String?)
	fun showCloudyError(shown: Boolean)
	fun showHumidityError(shown: Boolean)
	@StateStrategyType(OneExecutionStateStrategy::class)
	fun showSnackError()
}