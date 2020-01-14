package org.dhis2.utils

interface Validator {
    fun validate(text: String) : Boolean
}