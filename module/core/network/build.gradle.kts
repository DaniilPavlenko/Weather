import com.android.build.api.dsl.ApplicationDefaultConfig
import java.util.Properties

plugins {
    id("dpav.android.library")
    id("dpav.hilt.lib")
    id("dpav.hilt.ksp")
}

android {
    namespace = "ru.dpav.weather.core.network"

    defaultConfig.configureOpenWeatherApi()

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":module:core:model"))

    // Retrofit
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.retrofit)
}


private fun ApplicationDefaultConfig.configureOpenWeatherApi() {
    val propertiesFileName = "local.properties"
    val properties = Properties().apply {
        load(project.rootProject.file(propertiesFileName).inputStream())
    }

    val apiKeyPropertyName = "openweather.apikey"
    val openWeatherApiKey: String? = properties.getProperty(apiKeyPropertyName)

    check(openWeatherApiKey?.isNotBlank() == true) {
        "OpenWeather API configuration is missed. " +
            "Provide the API key in the '$apiKeyPropertyName' property " +
            "in the root '$propertiesFileName' file. " +
            "Don't have a key? Get it here - https://home.openweathermap.org/api_keys"
    }

    buildConfigField("String", "OPEN_WEATHER_API_KEY", "\"$openWeatherApiKey\"")
}
