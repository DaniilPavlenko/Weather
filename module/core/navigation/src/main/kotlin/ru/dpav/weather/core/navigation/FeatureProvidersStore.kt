package ru.dpav.weather.core.navigation

import kotlin.reflect.KClass

interface FeatureProvidersStore {
    fun <T : FeatureProvider> getFeatureProvider(providerClass: KClass<T>): T
}
