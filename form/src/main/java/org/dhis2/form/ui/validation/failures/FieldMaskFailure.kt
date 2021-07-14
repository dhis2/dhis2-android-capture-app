package org.dhis2.form.ui.validation.failures

sealed class FieldMaskFailure : Throwable() {
    object WrongPatternException : FieldMaskFailure()
}
