package ru.dpav.weather.feature.cities_list.impl

import androidx.fragment.app.Fragment
import ru.dpav.weather.feature.cities_list.api.StupidCitiesListFeatureProvider

class StupidCitiesListFeatureProviderImpl : StupidCitiesListFeatureProvider {
    override fun get(): Fragment = ListFragment.newInstance()
}
