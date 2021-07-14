package org.dhis2.form.ui.validation.validators

import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.valuetype.validation.validators.ValueTypeValidator

class FieldMaskValidator(val fieldMask: String) : ValueTypeValidator<FieldMaskFailure> {

    override fun validate(value: String): Result<String, FieldMaskFailure> {
        return when (value.matches(fieldMask.toRegex())) {
            true -> Result.Success(value)
            else -> Result.Failure(FieldMaskFailure.WrongPatternException)
        }
    }
}
