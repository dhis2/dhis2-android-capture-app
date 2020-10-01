package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator

class PositiveIntegerValidator : Validator {

    override fun validate(text: String): Boolean {
        return text.matches(regex)
    }

    companion object {
        val regex = Regex("[0-9]{1,10}")
    }
}