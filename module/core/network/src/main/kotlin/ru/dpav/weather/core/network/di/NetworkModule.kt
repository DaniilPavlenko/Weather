package ru.dpav.weather.core.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.dpav.weather.core.network.BuildConfig
import ru.dpav.weather.core.network.data.OpenWeatherDataSource
import ru.dpav.weather.core.network.WeatherDataSource
import ru.dpav.weather.core.network.data.api.OpenWeatherApiKeyInterceptor
import ru.dpav.weather.core.network.data.api.OpenWeatherService
import javax.inject.Singleton

private const val OPEN_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/"

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun openWeatherDataSource(impl: OpenWeatherDataSource): WeatherDataSource

    companion object {

        @Provides
        @Singleton
        fun openWeatherApiRetrofit(): OpenWeatherService {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(OpenWeatherApiKeyInterceptor(BuildConfig.OPEN_WEATHER_API_KEY))
                .build()
            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(OPEN_WEATHER_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(OpenWeatherService::class.java)
        }
    }
}
