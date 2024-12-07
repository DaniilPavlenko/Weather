package ru.dpav.weather.common.ui.extension

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType

fun View.consumeWindowInsets(
    @InsetsType insetTypes: Int,
    listener: (Insets) -> Unit,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        val insets = windowInsets.getInsets(insetTypes)
        listener(insets)
        WindowInsetsCompat.CONSUMED
    }
}
