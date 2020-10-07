package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator

class NumberValidator : Validator {

    override fun validate(text: String) = text.toDoubleOrNull()?.let { true } ?: false
}
