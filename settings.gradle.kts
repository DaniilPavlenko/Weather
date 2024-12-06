pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":app")
include(":module:common:icons")
include(":module:common:strings")
include(":module:core:data")
include(":module:core:model")
include(":module:core:network")
