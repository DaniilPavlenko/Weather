plugins {
    alias(libs.plugins.dpav.android.library)
}

android {
    namespace = "ru.dpav.weather.feature.cities_list.api"
}

dependencies {
    api(project(":module:core:navigation"))
}
