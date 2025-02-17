package org.dhis2.mobile.aggregates.data

interface ValueParser {
    fun parseValue(uid: String, value: String): String?
}
