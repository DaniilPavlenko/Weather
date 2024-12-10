package ru.dpav.weather.feature.cities_list.impl

import ru.dpav.weather.core.model.CityWeather

internal data class ListUiState(
    val citiesWeather: List<CityWeather>,
)
