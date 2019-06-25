package org.dhis2.data.tuples

import com.google.auto.value.AutoValue

@AutoValue
abstract class Quintet<A, B, C, D, E> {

    abstract fun val0(): A

    abstract fun val1(): B

    abstract fun val2(): C

    abstract fun val3(): D

    abstract fun val4(): E

    companion object {

        fun <A, B, C, D, E> create(val0: A,
                                   val1: B, val2: C,
                                   val3: D, val4: E): Quintet<A, B, C, D, E> {
            return AutoValue_Quintet(val0, val1, val2, val3, val4)
        }
    }
}
