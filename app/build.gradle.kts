import com.android.build.api.dsl.ApplicationDefaultConfig
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "ru.dpav.weather"
    compileSdk = 34
    defaultConfig {
        applicationId = "ru.dpav.weather"
        minSdk = 26
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        configureOpenWeatherApi()
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.play.services.location)

    // Retrofit
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.retrofit)

    // OpenStreetMap
    implementation(libs.osm.map)
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
