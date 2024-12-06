package ru.dpav.weather.feature.cities_list.impl

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.dpav.weather.core.data.WeatherRepository
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    weatherRepository: WeatherRepository
) : ViewModel() {

    val uiState = ListUiState(weatherRepository.citiesWeather)
}
