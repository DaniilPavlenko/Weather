package ru.dpav.core.model

@JvmInline
value class Speed private constructor(val metersPerSecond: Int) {
    companion object {
        fun metersPerSecond(meters: Int): Speed {
            return Speed(meters)
        }
    }
}
