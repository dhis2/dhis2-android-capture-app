package org.dhis2.mobile.commons.validation.validators

import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.valuetype.validation.validators.ValueTypeValidator
import java.util.regex.Pattern

class FieldMaskValidator(
    fieldMask: String,
) : ValueTypeValidator<FieldMaskFailure> {
    private val formattedFieldMask = fieldMask.removeSurrounding("\'")

    fun validateNullSafe(value: String?): Result<String?, FieldMaskFailure> = value?.let { validate(value) } ?: Result.Success(value)

    override fun validate(value: String): Result<String, FieldMaskFailure> =
        if (formattedFieldMask.isEmpty() || value.isEmpty()) {
            Result.Success(value)
        } else if (fieldMaskIsCorrect()) {
            if (value.matches(formattedFieldMask.toRegex())) {
                Result.Success(value)
            } else {
                Result.Failure(FieldMaskFailure.WrongPatternException)
            }
        } else {
            Result.Failure(FieldMaskFailure.InvalidPatternException)
        }

    private fun fieldMaskIsCorrect(): Boolean =
        try {
            Pattern.compile(formattedFieldMask)
            true
        } catch (e: Exception) {
            false
        }
}
