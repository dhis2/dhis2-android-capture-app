package org.dhis2.maps.model

sealed class AccuracyRange(
    open val value: Int,
) {
    data class None(
        override val value: Int = Int.MAX_VALUE,
    ) : AccuracyRange(value)

    data class Low(
        override val value: Int,
    ) : AccuracyRange(value)

    data class Medium(
        override val value: Int,
    ) : AccuracyRange(value)

    data class Good(
        override val value: Int,
    ) : AccuracyRange(value)

    data class VeryGood(
        override val value: Int,
    ) : AccuracyRange(value)
}

fun Float.toAccuracyRance() =
    when {
        this >= 100f -> AccuracyRange.Low(this.toInt())
        this > 25f -> AccuracyRange.Medium(this.toInt())
        this > 6 -> AccuracyRange.Good(this.toInt())
        else -> AccuracyRange.VeryGood(this.toInt())
    }
