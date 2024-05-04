package ru.dpav.weather.feature.cities_list

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.dpav.weather.api.model.City

@StateStrategyType(AddToEndSingleStrategy::class)
interface ListView : MvpView {
    fun updateCitiesList(cities: List<City>)
    fun toggleDropDownInfo(position: Int, shown: Boolean)
}
