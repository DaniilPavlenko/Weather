package ru.dpav.weather.core.navigation

import androidx.fragment.app.Fragment

interface NoArgsFeatureProvider : FeatureProvider {
    fun get(): Fragment
}
