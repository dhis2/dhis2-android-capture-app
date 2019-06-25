package org.dhis2.data.tuples

import com.google.auto.value.AutoValue

@AutoValue
abstract class Single<A> {

    abstract fun val0(): A

    companion object {

        fun <A> create(`val`: A): Single<A> {
            return AutoValue_Single(`val`)
        }
    }
}
