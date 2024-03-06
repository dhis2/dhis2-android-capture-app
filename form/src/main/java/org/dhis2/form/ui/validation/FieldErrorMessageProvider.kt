package org.dhis2.form.ui.validation

import android.content.Context
import org.dhis2.form.R
import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.BooleanFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.CoordinateFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.DateFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.DateTimeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.EmailFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerNegativeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerPositiveFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.LetterFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.NumberFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.PercentageFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.PhoneNumberFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.TextFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.TimeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.TrueOnlyFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.UnitIntervalFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.UrlFailure

class FieldErrorMessageProvider(private val context: Context) {

    fun getFriendlyErrorMessage(error: Throwable) =
        context.getString(parseErrorToMessageResource(error))

    private fun parseErrorToMessageResource(error: Throwable) = when (error) {
        is BooleanFailure -> getBooleanError(error)
        is DateFailure -> getDateError(error)
        is DateTimeFailure -> getDateTimeError(error)
        is LetterFailure -> getLetterError(error)
        is TextFailure -> getTextError(error)
        is TimeFailure -> getTimeError(error)
        is TrueOnlyFailure -> getTrueOnlyError(error)
        is PhoneNumberFailure -> getPhoneNumberError(error)
        is EmailFailure -> getEmailError(error)
        is IntegerNegativeFailure -> getIntegerNegativeError(error)
        is IntegerZeroOrPositiveFailure -> getIntegerZeroOrPositiveError(error)
        is IntegerPositiveFailure -> getIntegerPositiveError(error)
        is UnitIntervalFailure -> getUnitIntervalFailure(error)
        is PercentageFailure -> getPercentageError(error)
        is UrlFailure -> getUrlError(error)
        is IntegerFailure -> getIntegerError(error)
        is NumberFailure -> getNumberError(error)
        is FieldMaskFailure -> getFieldMaskError(error)
        is CoordinateFailure -> getCoordinateError(error)
        else -> R.string.invalid_field
    }

    private fun getTrueOnlyError(error: TrueOnlyFailure): Int {
        return when (error) {
            TrueOnlyFailure.BooleanMalformedException -> R.string.error_true_only_malformed
            TrueOnlyFailure.FalseIsNotAValidValueException ->
                R.string.error_true_only_false_not_valid
            TrueOnlyFailure.OneIsNotTrueException -> R.string.error_true_only_one_is_not_true
        }
    }

    private fun getTextError(error: TextFailure): Int {
        return when (error) {
            TextFailure.TooLargeTextException -> R.string.error_text_too_long
        }
    }

    private fun getLetterError(error: LetterFailure): Int {
        return when (error) {
            LetterFailure.EmptyStringException -> R.string.error_letter_empty
            LetterFailure.MoreThanOneLetterException -> R.string.error_letter_more_than_one
            LetterFailure.StringIsNotALetterException -> R.string.error_letter_not_a_letter
        }
    }

    private fun getTimeError(error: TimeFailure): Int {
        return when (error) {
            TimeFailure.ParseException -> R.string.error_time_parsing
        }
    }

    private fun getDateTimeError(error: DateTimeFailure): Int {
        return when (error) {
            DateTimeFailure.ParseException -> R.string.error_date_time_parsing
        }
    }

    private fun getDateError(error: DateFailure): Int {
        return when (error) {
            DateFailure.ParseException -> R.string.error_date_parsing
        }
    }

    private fun getBooleanError(error: BooleanFailure): Int {
        return when (error) {
            BooleanFailure.BooleanMalformedException -> R.string.error_boolean_malformed
            BooleanFailure.OneIsNotTrueException -> R.string.error_boolean_one_is_not_true
            BooleanFailure.ZeroIsNotFalseException -> R.string.error_boolean_zero_is_not_false
        }
    }

    private fun getCoordinateError(error: CoordinateFailure) = when (error) {
        CoordinateFailure.CoordinateMalformedException -> R.string.wrong_pattern
    }

    private fun getFieldMaskError(error: FieldMaskFailure) = when (error) {
        FieldMaskFailure.WrongPatternException -> R.string.wrong_pattern
        FieldMaskFailure.InvalidPatternException -> R.string.pattern_error
    }

    private fun getPhoneNumberError(error: PhoneNumberFailure) = when (error) {
        PhoneNumberFailure.MalformedPhoneNumberException -> R.string.invalid_phone_number
    }

    private fun getEmailError(error: EmailFailure) = when (error) {
        EmailFailure.MalformedEmailException -> R.string.invalid_email
    }

    private fun getIntegerNegativeError(error: IntegerNegativeFailure) = when (error) {
        IntegerNegativeFailure.IntegerOverflow -> R.string.formatting_error
        IntegerNegativeFailure.NumberFormatException -> R.string.formatting_error
        IntegerNegativeFailure.ValueIsPositive -> R.string.invalid_negative_number
        IntegerNegativeFailure.ValueIsZero -> R.string.invalid_negative_number
        IntegerNegativeFailure.LeadingZeroException -> R.string.leading_zero_error
    }

    private fun getIntegerZeroOrPositiveError(error: IntegerZeroOrPositiveFailure) = when (error) {
        IntegerZeroOrPositiveFailure.IntegerOverflow -> R.string.formatting_error
        IntegerZeroOrPositiveFailure.NumberFormatException -> R.string.formatting_error
        IntegerZeroOrPositiveFailure.ValueIsNegative -> R.string.invalid_possitive_zero
        IntegerZeroOrPositiveFailure.LeadingZeroException -> R.string.leading_zero_error
    }

    private fun getIntegerPositiveError(error: IntegerPositiveFailure) = when (error) {
        IntegerPositiveFailure.IntegerOverflow -> R.string.formatting_error
        IntegerPositiveFailure.NumberFormatException -> R.string.formatting_error
        IntegerPositiveFailure.ValueIsNegative -> R.string.invalid_possitive
        IntegerPositiveFailure.ValueIsZero -> R.string.invalid_possitive
        IntegerPositiveFailure.LeadingZeroException -> R.string.leading_zero_error
    }

    private fun getUnitIntervalFailure(error: UnitIntervalFailure) = when (error) {
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

    private fun getUrlError(error: UrlFailure) = when (error) {
        UrlFailure.MalformedUrlException -> R.string.validation_url
    }

    private fun getIntegerError(error: IntegerFailure) = when (error) {
        IntegerFailure.IntegerOverflow -> R.string.formatting_error
        IntegerFailure.NumberFormatException -> R.string.invalid_integer
        IntegerFailure.LeadingZeroException -> R.string.leading_zero_error
    }

    private fun getNumberError(error: NumberFailure) = when (error) {
        NumberFailure.NumberFormatException -> R.string.formatting_error
        NumberFailure.ScientificNotationException -> R.string.formatting_error
        NumberFailure.LeadingZeroException -> R.string.leading_zero_error
    }

    fun mandatoryWarning(): String {
        return context.getString(R.string.field_is_mandatory)
    }

    fun defaultValidationErrorMessage(): String {
        return context.getString(R.string.validation_error_message)
    }
}
