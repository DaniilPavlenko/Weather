package ru.dpav.weather.common.ui.extension

import androidx.fragment.app.FragmentManager

fun FragmentManager.popBackStackToRoot() = popBackStack(null, 0)
