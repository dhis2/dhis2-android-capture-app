package org.dhis2.data.tuples

import com.google.auto.value.AutoValue

@AutoValue
abstract class Pair<A, B> {

    abstract fun val0(): A

    abstract fun val1(): B

    companion object {

        fun <A, B> create(val0: A, val1: B): Pair<A, B> {
            return AutoValue_Pair(val0, val1)
        }
    }
}
