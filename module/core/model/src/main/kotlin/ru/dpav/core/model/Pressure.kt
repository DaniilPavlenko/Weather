package ru.dpav.core.model

import kotlin.math.roundToInt

private const val HECTOPASCALS_TO_MILLIMETERS_OF_MERCURY_DIVIDER = 1.333f

@JvmInline
value class Pressure private constructor(val millimetersOfMercury: Int) {
    companion object {
        fun hectopascals(hectopascals: Int): Pressure {
            return Pressure(
                (hectopascals / HECTOPASCALS_TO_MILLIMETERS_OF_MERCURY_DIVIDER).roundToInt()
            )
        }
    }
}
