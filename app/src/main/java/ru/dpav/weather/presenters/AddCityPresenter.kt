package ru.dpav.weather.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.util.Util
import ru.dpav.weather.views.AddCityView

@InjectViewState
class AddCityPresenter() : MvpPresenter<AddCityView>() {

    private lateinit var mCity: City
    var iconResId: Int = R.drawable.weather_icon_01

    constructor(city: City) : this() {
        if (city.id != 0) {
            mCity = CitiesRepository.customCities
                .first { it.id == city.id }

            iconResId = Util.Icons
                .getWeatherIconByName(mCity.weather[0].icon)
        } else {
            mCity = city
        }
    }

    fun onSave(city: City) {
        city.id = mCity.id
        city.coordinates = mCity.coordinates
        CitiesRepository.addCustomCity(city)
        viewState.save()
    }

    fun onNameError(message: String?) {
        viewState.showNameError(message)
    }

    fun onCloudyError(shown: Boolean) {
        viewState.showCloudyError(shown)
    }

    fun onHumidityError(shown: Boolean) {
        viewState.showHumidityError(shown)
    }

    fun onSaveError() {
        viewState.showSnackError()
    }

    fun onIconSelected(icon: Int) {
        iconResId = icon
        viewState.setWeatherIcon(icon)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (mCity.id != 0) {
            viewState.setCity(mCity)
            return
        }
        mCity.id = CitiesRepository.getNewCustomId()
    }
}
