plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    namespace = "ru.dpav.weather"
    compileSdk = 31
    defaultConfig {
        applicationId = "ru.dpav.weather"
        minSdk = 26
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.retrofit.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.androidx.legacy.support.v4)

    //MOXY
    implementation(libs.moxy.androidx)
    implementation(libs.moxy.moxy)
    kapt(libs.moxy.compiler)

    //OSMdroid
    implementation(libs.osm.map)
    implementation(libs.osm.bonuspack)

    implementation(libs.rx.kotlin)
    implementation(libs.rx.android)
    implementation(libs.retrofit.adapter.rxjava2)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.junit)
    implementation(libs.androidx.gridlayout)
}
