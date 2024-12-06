package ru.dpav.weather.core.navigation

import androidx.fragment.app.Fragment

interface FeatureProvider

inline fun <reified T : FeatureProvider> Fragment.findFeatureProvider(): T {
    val application = requireContext().applicationContext
    check(application is FeatureProvidersStore) {
        "$application must implement FeatureProviderStore."
    }
    return application.getFeatureProvider(T::class)
}
