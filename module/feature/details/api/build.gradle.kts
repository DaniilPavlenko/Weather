plugins {
    alias(libs.plugins.dpav.android.library)
}

android {
    namespace = "ru.dpav.weather.feature.details.api"
}

dependencies {
    api(project(":module:core:navigation"))
}
