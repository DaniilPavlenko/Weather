package ru.dpav.core.model

@JvmInline
value class Pressure private constructor(val millimetersOfMercury: Int) {
    companion object {
        fun millimetersOfMercury(pressure: Int): Pressure {
            return Pressure(pressure)
        }
    }
}
