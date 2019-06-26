package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.api.City
import ru.dpav.weather.views.AddCityView

@InjectViewState
class AddCityPresenter : MvpPresenter<AddCityView>() {
	private lateinit var mCity: City

	fun onDataChanged(city: City) {
		mCity = city
	}

	fun onSave(city: City) {
		CitiesRepository.addCustomCity(city)
		viewState.save()
	}

	override fun onFirstViewAttach() {
		super.onFirstViewAttach()
		mCity = City.getEmpty()
		mCity.id = CUSTOM_CITY_ID_OFFSET +
			CitiesRepository.customCities.size
		viewState.setCity(mCity)
	}

	companion object {
		private const val CUSTOM_CITY_ID_OFFSET = 20000000
	}
}