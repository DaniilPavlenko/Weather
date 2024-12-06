package ru.dpav.weather.core.navigation

import androidx.fragment.app.Fragment

interface Navigator {
    fun Fragment.navigateTo(
        destinationFragment: Fragment,
        tag: String,
        shouldAddToBackStack: Boolean = true,
    )
}

fun Fragment.findNavigator(): Navigator {
    val activity = requireActivity()
    check(activity is Navigator) { "$activity must implement Navigator." }
    return activity
}
