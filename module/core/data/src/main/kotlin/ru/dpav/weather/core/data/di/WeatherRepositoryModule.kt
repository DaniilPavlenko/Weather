package ru.dpav.weather.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.dpav.weather.core.data.WeatherRepository
import ru.dpav.weather.core.data.impl.WeatherRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WeatherRepositoryModule {

    @Binds
    @Singleton
    abstract fun weatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

}
