plugins {
    alias(libs.plugins.dpav.android.library)
    alias(libs.plugins.dpav.hilt.lib)
    alias(libs.plugins.dpav.hilt.ksp)
}

android {
    namespace = "ru.dpav.weather.feature.details.impl"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(project(":module:feature:details:api"))

    implementation(project(":module:common:strings"))
    implementation(project(":module:common:ui"))
    implementation(project(":module:core:data"))

    implementation(libs.material)
}
