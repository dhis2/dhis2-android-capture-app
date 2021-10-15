package org.dhis2.data.forms.dataentry.fields.edittext

interface PatterValidator {
    fun onSuccess()
    fun onError()
    fun onPatternError()
}
