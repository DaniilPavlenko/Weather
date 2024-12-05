import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "ru.dpav.build_logic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "dpav.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "dpav.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidHiltLib") {
            id = "dpav.hilt.lib"
            implementationClass = "AndroidHiltLibConventionPlugin"
        }
        register("andoridHiltKsp") {
            id = "dpav.hilt.ksp"
            implementationClass = "AndroidHiltKspConventionPlugin"
        }
    }
}
