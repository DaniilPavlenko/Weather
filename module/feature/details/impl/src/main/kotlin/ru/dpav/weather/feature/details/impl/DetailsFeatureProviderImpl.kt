package ru.dpav.weather.feature.details.impl

import androidx.fragment.app.Fragment
import ru.dpav.weather.feature.details.api.DetailsFeatureProvider

class DetailsFeatureProviderImpl : DetailsFeatureProvider {
    override fun get(cityId: Int): Fragment = CityDetailsFragment.newInstance(cityId)
}
