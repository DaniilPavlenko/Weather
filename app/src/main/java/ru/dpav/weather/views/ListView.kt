package ru.dpav.weather.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.dpav.weather.api.City

@StateStrategyType(AddToEndSingleStrategy::class)
interface ListView : MvpView {
	fun updateCitiesList(cities: List<City>)
	fun showDropDownInfo(position: Int)
	fun hideDropDownInfo()
}