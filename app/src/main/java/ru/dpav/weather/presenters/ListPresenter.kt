package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.api.City
import ru.dpav.weather.views.ListView
import java.util.*

@InjectViewState
class ListPresenter() : MvpPresenter<ListView>(), Observer {

	private var mCode: Int = 0

	constructor(code: Int) : this() {
		mCode = code
	}

	fun onToggleDropDownInfo(position: Int, shown: Boolean) {
		viewState.toggleDropDownInfo(position, shown)
	}

	override fun onFirstViewAttach() {
		if (mCode == 0) {
			CitiesRepository.addObserver(this)
		}
	}

	override fun update(observable: Observable?, arg: Any?) {
		val cityRep = observable as CitiesRepository
		val cities = arrayListOf<City>()
		cities.addAll(cityRep.cities)
		cities.addAll(cityRep.customCities)
		viewState.updateCitiesList(cities)
	}
}