package ru.dpav.weather.feature.cities_list.impl

import androidx.fragment.app.Fragment
import ru.dpav.weather.feature.cities_list.api.CitiesListFeatureProvider

class CitiesListFeatureProviderImpl : CitiesListFeatureProvider {
    override fun get(): Fragment = ListFragment.newInstance()
}
