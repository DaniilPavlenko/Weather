plugins {
    alias(libs.plugins.dpav.android.library)
}

android {
    namespace = "ru.dpav.weather.common.ui"
}

dependencies {
    implementation(project(":module:common:icons"))
    implementation(project(":module:core:model"))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.fragment.ktx)
}
