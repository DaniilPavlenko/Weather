package ru.dpav.weather.navigation

import ru.dpav.weather.core.navigation.FeatureProvider
import ru.dpav.weather.core.navigation.FeatureProvidersStore
import ru.dpav.weather.feature.cities_list.api.StupidCitiesListFeatureProvider
import ru.dpav.weather.feature.cities_list.impl.StupidCitiesListFeatureProviderImpl
import ru.dpav.weather.feature.details.api.StupidDetailsFeatureProvider
import ru.dpav.weather.feature.details.impl.StupidDetailsFeatureProviderImpl
import kotlin.reflect.KClass

class FeatureProviderImplementationsMap : FeatureProvidersStore {

    private val featureProvidersMap by lazy {
        mapOf(
            StupidDetailsFeatureProvider::class to StupidDetailsFeatureProviderImpl(),
            StupidCitiesListFeatureProvider::class to StupidCitiesListFeatureProviderImpl()
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : FeatureProvider> getFeatureProvider(providerClass: KClass<T>): T {
        val provider = featureProvidersMap[providerClass]
            ?: error("Implementation for $providerClass isn't provided.")
        return provider as T
    }
}
