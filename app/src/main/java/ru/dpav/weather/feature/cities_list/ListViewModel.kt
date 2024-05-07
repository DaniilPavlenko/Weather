package ru.dpav.weather.feature.cities_list

import androidx.lifecycle.ViewModel
import ru.dpav.weather.data.WeatherRepository

class ListViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository

    val uiState = ListUiState(weatherRepository.cities)
}
