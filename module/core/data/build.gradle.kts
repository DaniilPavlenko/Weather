plugins {
    id("dpav.android.library")
    id("dpav.hilt.lib")
    id("dpav.hilt.ksp")
}

android {
    namespace = "ru.dpav.weather.core.data"
}

dependencies {
    implementation(project(":module:core:model"))
    implementation(project(":module:core:network"))
}
