plugins {
    alias(libs.plugins.dpav.android.application)
    alias(libs.plugins.dpav.hilt.lib)
    alias(libs.plugins.dpav.hilt.ksp)
}

android {
    namespace = "ru.dpav.weather"

    defaultConfig {
        applicationId = "ru.dpav.weather"
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
    implementation(project(":module:feature:cities-list:impl"))
    implementation(project(":module:feature:details:impl"))
    implementation(project(":module:feature:map"))

    implementation(libs.androidx.appcompat)
}
