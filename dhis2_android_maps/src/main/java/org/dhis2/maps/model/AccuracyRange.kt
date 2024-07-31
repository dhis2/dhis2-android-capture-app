package org.dhis2.maps.model

sealed class AccuracyRange(open val value: Int) {
    data class None(override val value: Int = 0) : AccuracyRange(value)
    data class Low(override val value: Int) : AccuracyRange(value)
    data class Medium(override val value: Int) : AccuracyRange(value)
    data class Good(override val value: Int) : AccuracyRange(value)
    data class VeryGood(override val value: Int) : AccuracyRange(value)
}
