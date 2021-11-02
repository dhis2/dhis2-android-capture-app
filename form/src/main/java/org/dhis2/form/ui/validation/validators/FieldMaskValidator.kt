package org.dhis2.form.ui.validation.validators

import java.lang.Exception
import java.util.regex.Pattern
import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.valuetype.validation.validators.ValueTypeValidator
import timber.log.Timber

class FieldMaskValidator(val fieldMask: String) : ValueTypeValidator<FieldMaskFailure> {

    override fun validate(value: String): Result<String, FieldMaskFailure> {
        return if (value.isEmpty()) {
            Result.Success(value)
        } else if (fieldMaskIsCorrect()) {
            if (value.matches(fieldMask.toRegex())) {
                Result.Success(value)
            } else {
                Result.Failure(FieldMaskFailure.WrongPatternException)
            }
        } else {
            Result.Failure(FieldMaskFailure.InvalidPatternException)
        }
    }

    private fun fieldMaskIsCorrect(): Boolean {
        return try {
            Pattern.compile(fieldMask)
            true
        } catch (e: Exception) {
            Timber.d(e)
            false
        }
    }
}
