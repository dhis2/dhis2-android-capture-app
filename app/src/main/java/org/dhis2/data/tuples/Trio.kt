package org.dhis2.data.tuples

import com.google.auto.value.AutoValue

@AutoValue
abstract class Trio<A, B, C> {

    abstract fun val0(): A?

    abstract fun val1(): B?

    abstract fun val2(): C?

    companion object {

        fun <A, B, C> create(val0: A?, val1: B?, val2: C?): Trio<A, B, C> {
            return AutoValue_Trio(val0, val1, val2)
        }
    }
}
