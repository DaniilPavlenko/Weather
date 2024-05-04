package ru.dpav.weather.feature.cities_list

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.CitiesRepository
import java.util.Observable
import java.util.Observer

@InjectViewState
class ListPresenter : MvpPresenter<ListView>(), Observer {

    private val citiesRepository = CitiesRepository

    fun onToggleDropDownInfo(
        position: Int,
        shown: Boolean,
    ) {
        viewState.toggleDropDownInfo(position, shown)
    }

    override fun onFirstViewAttach() {
        citiesRepository.addObserver(this)
    }

    override fun update(observable: Observable?, arg: Any?) {
        viewState.updateCitiesList(citiesRepository.cities)
    }
}
