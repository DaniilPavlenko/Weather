package ru.dpav.weather.core.navigation

import androidx.fragment.app.Fragment

interface FeatureProvider

/**
 * ⚠️ Don't keep a reference to this provider for too long.
 *
 * It's an [android.app.Activity] under the hood.
 */
inline fun <reified T : FeatureProvider> Fragment.findFeatureProvider(): T {
    val activity = requireActivity()
    check(activity is T) {
        "Activity that hosted $this must implement ${T::class.simpleName}"
    }
    return activity
}
