package ru.dpav.weather

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.feature.details.api.StupidDetailsFeatureProvider
import ru.dpav.weather.feature.details.impl.StupidDetailsFeatureProviderImpl

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(R.layout.activity_main),
    StupidDetailsFeatureProvider by StupidDetailsFeatureProviderImpl()
