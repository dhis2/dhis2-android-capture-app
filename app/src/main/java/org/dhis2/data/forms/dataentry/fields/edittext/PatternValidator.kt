package org.dhis2.data.forms.dataentry.fields.edittext

interface PatternValidator {
    fun onSuccess()
    fun onError()
    fun onPatternError()
}
