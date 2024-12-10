plugins {
    alias(libs.plugins.dpav.android.library)
}

android {
    namespace = "ru.dpav.weather.core.navigation"
}

dependencies {
    api(libs.androidx.fragment.ktx)
}
