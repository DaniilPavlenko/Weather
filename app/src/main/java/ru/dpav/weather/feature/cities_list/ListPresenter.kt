package ru.dpav.weather.feature.cities_list

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.data.WeatherRepository

@InjectViewState
class ListPresenter : MvpPresenter<ListView>() {

    private val weatherRepository = WeatherRepository

    fun onToggleDropDownInfo(
        position: Int,
        shown: Boolean,
    ) {
        viewState.toggleDropDownInfo(position, shown)
    }

    override fun attachView(view: ListView?) {
        super.attachView(view)
        viewState.updateCitiesList(weatherRepository.cities)
    }
}
