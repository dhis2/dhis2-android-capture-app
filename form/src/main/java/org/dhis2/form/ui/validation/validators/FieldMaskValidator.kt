package org.dhis2.form.ui.validation.validators

import androidx.annotation.VisibleForTesting
import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.valuetype.validation.validators.ValueTypeValidator
import timber.log.Timber
import java.util.regex.Pattern

class FieldMaskValidator(val fieldMask: String?) : ValueTypeValidator<FieldMaskFailure> {

    private val formattedFieldMask = fieldMask?.removeSurrounding("\'")

    fun validateNullSafe(value: String?): Result<String?, FieldMaskFailure> {
        return value?.let { validate(value) } ?: Result.Success(value)
    }

    override fun validate(value: String): Result<String, FieldMaskFailure> {
        return if (formattedFieldMask.isNullOrEmpty() || value.isEmpty()) {
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
    }

    @VisibleForTesting
    fun fieldMaskIsCorrect(): Boolean {
        return try {
            Pattern.compile(formattedFieldMask)
            true
        } catch (e: Exception) {
            Timber.d(e)
            false
        }
    }
}
