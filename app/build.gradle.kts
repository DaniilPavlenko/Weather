plugins {
    id("dpav.android.application")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "ru.dpav.weather"

    defaultConfig {
        applicationId = "ru.dpav.weather"
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            // Just for simplicity. In real projects I read 'keystore.properties'.
            signingConfig = signingConfigs.named("debug").get()

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":module:core:data"))
    implementation(project(":module:core:model"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.play.services.location)

    // OpenStreetMap
    implementation(libs.osm.map)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
