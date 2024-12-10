plugins {
    alias(libs.plugins.dpav.android.library)
    alias(libs.plugins.dpav.hilt.lib)
    alias(libs.plugins.dpav.hilt.ksp)
}

android {
    namespace = "ru.dpav.weather.feature.cities_list.impl"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(project(":module:feature:cities-list:api"))

    implementation(project(":module:common:strings"))
    implementation(project(":module:common:ui"))
    implementation(project(":module:core:data"))
    implementation(project(":module:core:model"))
    implementation(project(":module:feature:details:api"))

    implementation(libs.material)
}
