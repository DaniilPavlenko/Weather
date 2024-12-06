package ru.dpav.weather.feature.details.impl

import androidx.fragment.app.Fragment
import ru.dpav.weather.feature.details.api.StupidDetailsFeatureProvider

class StupidDetailsFeatureProviderImpl : StupidDetailsFeatureProvider {
    override fun get(cityId: Int): Fragment = CityDetailsFragment.newInstance(cityId)
}
