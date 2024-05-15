package ru.dpav.weather.ui.extension

import androidx.fragment.app.FragmentManager

fun FragmentManager.popBackStackToRoot() = popBackStack(null, 0)
