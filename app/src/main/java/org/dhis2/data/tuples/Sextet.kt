package org.dhis2.data.tuples

import com.google.auto.value.AutoValue

@AutoValue
abstract class Sextet<A, B, C, D, E, F> {

    abstract fun val0(): A

    abstract fun val1(): B

    abstract fun val2(): C

    abstract fun val3(): D

    abstract fun val4(): E

    abstract fun val5(): F

    companion object {

        fun <A, B, C, D, E, F> create(val0: A,
                                      val1: B, val2: C,
                                      val3: D, val4: E, val5: F): Sextet<A, B, C, D, E, F> {
            return AutoValue_Sextet(val0, val1, val2, val3, val4, val5)
        }
    }
}
