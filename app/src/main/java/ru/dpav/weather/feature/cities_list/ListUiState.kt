package ru.dpav.weather.feature.cities_list

import ru.dpav.weather.core.network.data.model.City

data class ListUiState(
    val cities: List<City>,
)
