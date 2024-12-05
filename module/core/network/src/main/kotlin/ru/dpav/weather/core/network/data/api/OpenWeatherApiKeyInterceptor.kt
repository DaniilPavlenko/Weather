package ru.dpav.weather.core.network.data.api

import okhttp3.Interceptor
import okhttp3.Response

internal class OpenWeatherApiKeyInterceptor(
    private val openWeatherApiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val urlWithApiKey = originalRequest.url()
            .newBuilder()
            .addQueryParameter("appid", openWeatherApiKey)
            .build()
        val authorizedRequest = originalRequest
            .newBuilder()
            .url(urlWithApiKey)
            .build()
        val response = chain.proceed(authorizedRequest)
        return response
    }
}
