package ru.dpav.build_logic

import ANDROID_COMPILE_SDK
import ANDROID_MIN_SDK
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    with(commonExtension) {
        compileSdk = ANDROID_COMPILE_SDK
        defaultConfig {
            minSdk = ANDROID_MIN_SDK
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
    configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}
