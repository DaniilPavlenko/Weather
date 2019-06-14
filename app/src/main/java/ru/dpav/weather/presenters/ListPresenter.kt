package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.api.City
import ru.dpav.weather.views.ListView

@InjectViewState
class ListPresenter : MvpPresenter<ListView>() {
	fun onCitiesUpdate(cities: List<City>) {
		viewState.updateCitiesList(cities)
	}

	companion object {
		const val TAG_PRESENTER = "listPresenter"
	}
}