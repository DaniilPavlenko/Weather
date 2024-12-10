plugins {
    alias(libs.plugins.dpav.android.library)
    alias(libs.plugins.dpav.hilt.lib)
    alias(libs.plugins.dpav.hilt.ksp)
}

android {
    namespace = "ru.dpav.weather.core.data"
}

dependencies {
    implementation(project(":module:core:model"))
    implementation(project(":module:core:network"))
}
