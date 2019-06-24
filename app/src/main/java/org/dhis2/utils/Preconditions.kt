package org.dhis2.utils

import java.util.Objects

object Preconditions {

    fun <T> isNull(obj: T?, message: String): T {
        if (obj == null) {
            throw IllegalArgumentException(message)
        }

        return obj
    }

    fun equals(one: Any?, two: Any?): Boolean {
        return one == two // NOPMD
    }
}// no instances