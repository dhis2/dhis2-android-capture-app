package org.dhis2.utils

class Preconditions {

    companion object {

        @JvmStatic
        fun <T> isNull(obj: T?, message: String) =
            obj?.let { it } ?: throw IllegalArgumentException(message)

        @JvmStatic
        fun equals(one: Any?, two: Any?) = one == two || (one != null && one == two) // NOPMD
    }
}
