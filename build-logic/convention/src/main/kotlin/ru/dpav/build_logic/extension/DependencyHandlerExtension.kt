package ru.dpav.build_logic.extension

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope
import kotlin.jvm.optionals.getOrNull

fun DependencyHandlerScope.addImplementation(name: String, versionCatalog: VersionCatalog) {
    add(
        "implementation",
        versionCatalog.findLibrary(name).getOrNull() ?: {
            throw IllegalArgumentException("Cannot found $name in the passed version catalog.")
        }
    )
}
