plugins {
    alias(libs.plugins.dpav.android.library)
    alias(libs.plugins.dpav.hilt.lib)
    alias(libs.plugins.dpav.hilt.ksp)
}

android {
    namespace = "ru.dpav.weather.feature.map"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":module:common:ui"))
    implementation(project(":module:core:data"))
    implementation(project(":module:core:model"))
    implementation(project(":module:feature:cities-list:api"))
    implementation(project(":module:feature:details:api"))

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.osm.map)
    implementation(libs.play.services.location)
}
