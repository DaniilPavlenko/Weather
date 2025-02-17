package ru.dpav.weather.feature.details.api

import androidx.fragment.app.Fragment
import ru.dpav.weather.core.navigation.FeatureProvider

interface DetailsFeatureProvider : FeatureProvider {
    fun get(cityId: Int): Fragment
}
