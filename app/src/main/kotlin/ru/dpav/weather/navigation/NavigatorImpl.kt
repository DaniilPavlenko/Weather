package ru.dpav.weather.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import ru.dpav.weather.R
import ru.dpav.weather.core.navigation.Navigator

internal class NavigatorImpl : Navigator {
    override fun Fragment.navigateTo(
        destinationFragment: Fragment,
        tag: String,
        shouldAddToBackStack: Boolean
    ) {
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, destinationFragment, tag)
            if (shouldAddToBackStack) {
                addToBackStack(tag)
            }
        }
    }

    override fun Fragment.navigateBack() {
        parentFragmentManager.popBackStack()
    }

    override fun Fragment.navigateBackToRoot() {
        parentFragmentManager.popBackStack(null, 0)
    }
}
