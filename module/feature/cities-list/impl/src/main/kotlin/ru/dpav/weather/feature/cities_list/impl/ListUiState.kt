package ru.dpav.weather.feature.cities_list.impl

import ru.dpav.weather.core.model.CityWeather

data class ListUiState(
    val citiesWeather: List<CityWeather>,
)
