plugins {
    id("dpav.android.library")
}

android {
    namespace = "ru.dpav.weather.core.network"
}

dependencies {
    implementation(project(":module:core:model"))

    // Retrofit
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.retrofit)
}
