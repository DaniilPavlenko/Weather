package ru.dpav.weather.navigation

import ru.dpav.weather.core.navigation.FeatureProvider
import ru.dpav.weather.core.navigation.FeatureProvidersStore
import ru.dpav.weather.feature.cities_list.api.CitiesListFeatureProvider
import ru.dpav.weather.feature.cities_list.impl.CitiesListFeatureProviderImpl
import ru.dpav.weather.feature.details.api.DetailsFeatureProvider
import ru.dpav.weather.feature.details.impl.DetailsFeatureProviderImpl
import kotlin.reflect.KClass

class FeatureProviderImplementationsMap : FeatureProvidersStore {

    private val featureProvidersMap by lazy {
        mapOf(
            DetailsFeatureProvider::class to DetailsFeatureProviderImpl(),
            CitiesListFeatureProvider::class to CitiesListFeatureProviderImpl()
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : FeatureProvider> getFeatureProvider(providerClass: KClass<T>): T {
        val provider = featureProvidersMap[providerClass]
            ?: error("Implementation for $providerClass isn't provided.")
        return provider as T
    }
}
