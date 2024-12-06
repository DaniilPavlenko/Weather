package ru.dpav.weather.feature.details.api

import androidx.fragment.app.Fragment
import ru.dpav.weather.core.navigation.FeatureProvider

interface StupidDetailsFeatureProvider : FeatureProvider {
    fun get(cityId: Int): Fragment
}
