package org.dhis2.form.ui

import android.content.Context
import org.dhis2.form.R
import org.hisp.dhis.android.core.common.valuetype.validation.failures.EmailFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerNegativeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerPositiveFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.PercentageFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.PhoneNumberFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.UnitIntervalFailure

class FieldErrorMessageProvider(private val context: Context) {

    fun getFriendlyErrorMessage(error: Throwable) =
        context.getString(parseErrorToMessageResource(error))

    private fun parseErrorToMessageResource(error: Throwable) =
        when (error) {
            is PhoneNumberFailure -> getPhoneNumberError(error)
            is EmailFailure -> getEmailError(error)
            is IntegerNegativeFailure -> getIntegerNegativeError(error)
            is IntegerZeroOrPositiveFailure -> getIntegerZeroOrPositiveError(error)
            is IntegerPositiveFailure -> getIntegerPositiveError(error)
            is UnitIntervalFailure -> getUnitIntervalFailure(error)
            is PercentageFailure -> getPercentageError(error)
            else -> R.string.invalid_field
        }

    private fun getPhoneNumberError(error: PhoneNumberFailure) =
        when (error) {
            PhoneNumberFailure.MalformedPhoneNumberException -> R.string.invalid_phone_number
        }

    private fun getEmailError(error: EmailFailure) =
        when (error) {
            EmailFailure.MalformedEmailException -> R.string.invalid_email
        }

    private fun getIntegerNegativeError(error: IntegerNegativeFailure) =
        when (error) {
            IntegerNegativeFailure.IntegerOverflow -> R.string.formatting_error
            IntegerNegativeFailure.NumberFormatException -> R.string.formatting_error
            IntegerNegativeFailure.ValueIsPositive -> R.string.invalid_negative_number
            IntegerNegativeFailure.ValueIsZero -> R.string.invalid_negative_number
        }

    private fun getIntegerZeroOrPositiveError(error: IntegerZeroOrPositiveFailure) =
        when (error) {
            IntegerZeroOrPositiveFailure.IntegerOverflow -> R.string.formatting_error
            IntegerZeroOrPositiveFailure.NumberFormatException -> R.string.formatting_error
            IntegerZeroOrPositiveFailure.ValueIsNegative -> R.string.invalid_possitive_zero
        }

    private fun getIntegerPositiveError(error: IntegerPositiveFailure) =
        when (error) {
            IntegerPositiveFailure.IntegerOverflow -> R.string.formatting_error
            IntegerPositiveFailure.NumberFormatException -> R.string.formatting_error
            IntegerPositiveFailure.ValueIsNegative -> R.string.invalid_possitive
            IntegerPositiveFailure.ValueIsZero -> R.string.invalid_possitive
        }

    private fun getUnitIntervalFailure(error: UnitIntervalFailure) =
        when (error) {
            UnitIntervalFailure.GreaterThanOneException -> R.string.invalid_interval
            UnitIntervalFailure.NumberFormatException -> R.string.formatting_error
            UnitIntervalFailure.ScientificNotationException -> R.string.formatting_error
            UnitIntervalFailure.SmallerThanZeroException -> R.string.invalid_interval
        }

    private fun getPercentageError(error: PercentageFailure) = when (error) {
        PercentageFailure.NumberFormatException -> R.string.formatting_error
        PercentageFailure.ValueGreaterThan100 -> R.string.invalid_percentage
        PercentageFailure.ValueIsNegative -> R.string.invalid_possitive
    }
}