plugins {
    id("dpav.android.library")
    id("dpav.hilt.lib")
    id("dpav.hilt.ksp")
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
