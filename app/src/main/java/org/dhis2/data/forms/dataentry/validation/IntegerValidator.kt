package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator

class IntegerValidator : Validator {

    override fun validate(text: String) = text.toIntOrNull()?.let { true } ?: false
}
