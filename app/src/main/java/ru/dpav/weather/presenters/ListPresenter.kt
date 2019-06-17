package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.api.City
import ru.dpav.weather.views.ListView

@InjectViewState
class ListPresenter() : MvpPresenter<ListView>() {
	private var mCode: Int = 0

	constructor(code: Int) : this() {
		mCode = code
	}

	fun onCitiesUpdate(cities: List<City>) {
		viewState.updateCitiesList(cities)
	}

	fun onShowDropDownInfo(position: Int) {
		viewState.showDropDownInfo(position)
	}

	fun onHideDropDownInfo() {
		viewState.hideDropDownInfo()
	}

	companion object {
		const val TAG_PRESENTER = "listPresenter"
	}
}