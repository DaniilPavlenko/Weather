package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.api.City
import ru.dpav.weather.views.AddCityView

@InjectViewState
class AddCityPresenter : MvpPresenter<AddCityView>() {

	private lateinit var mCity: City

	fun onSave(city: City) {
		city.id = mCity.id
		city.coordinates = mCity.coordinates
		CitiesRepository.addCustomCity(city)
		viewState.save()
	}

	fun setCity(city: City) {
		mCity = city
	}

	fun onRemoveClick() {
		viewState.showRemoveDialog(true)
	}

	fun onAcceptDialog() {
		CitiesRepository.removeCustomCity(mCity)
	}

	fun onDeclineDialog() {
		viewState.showRemoveDialog(false)
	}

	override fun onFirstViewAttach() {
		super.onFirstViewAttach()
		if (mCity.id != 0) {
			viewState.setCity(mCity)
			return
		}
		mCity.id = CUSTOM_CITY_ID_OFFSET +
			CitiesRepository.customCities.size
	}

	companion object {
		private const val CUSTOM_CITY_ID_OFFSET = 20000000
	}
}