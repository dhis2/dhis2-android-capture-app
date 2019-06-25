package org.dhis2.data.tuples

import com.google.auto.value.AutoValue

@AutoValue
abstract class Quartet<A, B, C, D> {

    abstract fun val0(): A

    abstract fun val1(): B

    abstract fun val2(): C

    abstract fun val3(): D

    companion object {

        fun <A, B, C, D> create(val0: A, val1: B,
                                val2: C, val3: D): Quartet<A, B, C, D> {
            return AutoValue_Quartet(val0, val1, val2, val3)
        }
    }
}
