package ru.dpav.weather.api

import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.dpav.weather.BuildConfig
import ru.dpav.weather.api.interceptor.OpenWeatherApiKeyInterceptor

object WeatherApi {
    private const val API_BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val api: OpenWeatherService by lazy {
        val retrofit = Retrofit.Builder()
            .client(createHttpClient())
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
        retrofit.create(OpenWeatherService::class.java)
    }

    private fun createHttpClient() = OkHttpClient.Builder()
        .addInterceptor(OpenWeatherApiKeyInterceptor(BuildConfig.OPEN_WEATHER_API_KEY))
        .build()
}
