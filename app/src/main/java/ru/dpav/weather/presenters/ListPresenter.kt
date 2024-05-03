package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.api.City
import ru.dpav.weather.views.ListView
import java.util.Observable
import java.util.Observer

@InjectViewState
class ListPresenter : MvpPresenter<ListView>(), Observer {

    fun onToggleDropDownInfo(
        position: Int,
        shown: Boolean,
    ) {
        viewState.toggleDropDownInfo(position, shown)
    }

    override fun onFirstViewAttach() {
        CitiesRepository.addObserver(this)
    }

    override fun update(
        observable: Observable?,
        arg: Any?,
    ) {
        val cityRep = observable as CitiesRepository
        val cities = arrayListOf<City>()
        cities.addAll(cityRep.cities)
        cities.addAll(cityRep.customCities)
        viewState.updateCitiesList(cities)
    }
}
