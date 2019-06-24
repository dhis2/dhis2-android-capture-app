package org.dhis2.utils

import com.google.auto.value.AutoValue

import java.util.ArrayList

@AutoValue
abstract class Result<T> {

    abstract fun items(): List<T>

    abstract fun error(): Exception?

    companion object {

        fun <E> success(items: List<E>): Result<E> {
            return AutoValue_Result(items, null)
        }

        fun failure(exception: Exception): Result<*> {
            return AutoValue_Result(ArrayList<Any>(), exception)
        }
    }
}
