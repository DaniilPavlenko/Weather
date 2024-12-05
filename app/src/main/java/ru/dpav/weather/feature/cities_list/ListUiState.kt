package ru.dpav.weather.feature.cities_list

import ru.dpav.weather.core.model.CityWeather

data class ListUiState(
    val citiesWeather: List<CityWeather>,
)
