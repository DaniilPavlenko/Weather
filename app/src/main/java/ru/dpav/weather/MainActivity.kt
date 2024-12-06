package ru.dpav.weather

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.core.navigation.Navigator
import ru.dpav.weather.feature.cities_list.api.StupidCitiesListFeatureProvider
import ru.dpav.weather.feature.cities_list.impl.StupidCitiesListFeatureProviderImpl
import ru.dpav.weather.feature.details.api.StupidDetailsFeatureProvider
import ru.dpav.weather.feature.details.impl.StupidDetailsFeatureProviderImpl

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    Navigator by NavigatorImpl(),
    StupidCitiesListFeatureProvider by StupidCitiesListFeatureProviderImpl(),
    StupidDetailsFeatureProvider by StupidDetailsFeatureProviderImpl()
