package org.dhis2.form.ui.validation.validators

interface PatternValidator {
    fun onSuccess()
    fun onError()
    fun onPatternError()
}
