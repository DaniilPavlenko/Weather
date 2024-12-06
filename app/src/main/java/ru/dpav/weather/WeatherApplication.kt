package ru.dpav.weather

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.dpav.weather.core.navigation.FeatureProvidersStore
import ru.dpav.weather.navigation.FeatureProviderImplementationsMap

@HiltAndroidApp
class WeatherApplication : Application(),
    FeatureProvidersStore by FeatureProviderImplementationsMap()
