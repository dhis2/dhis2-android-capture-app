package org.dhis2.mobile.commons.providers

import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.custom_intent_error
import org.dhis2.mobile.commons.resources.error_boolean_malformed
import org.dhis2.mobile.commons.resources.error_boolean_one_is_not_true
import org.dhis2.mobile.commons.resources.error_boolean_zero_is_not_false
import org.dhis2.mobile.commons.resources.error_date_parsing
import org.dhis2.mobile.commons.resources.error_date_time_parsing
import org.dhis2.mobile.commons.resources.error_letter_empty
import org.dhis2.mobile.commons.resources.error_letter_more_than_one
import org.dhis2.mobile.commons.resources.error_letter_not_a_letter
import org.dhis2.mobile.commons.resources.error_text_too_long
import org.dhis2.mobile.commons.resources.error_time_parsing
import org.dhis2.mobile.commons.resources.error_true_only_false_not_valid
import org.dhis2.mobile.commons.resources.error_true_only_malformed
import org.dhis2.mobile.commons.resources.error_true_only_one_is_not_true
import org.dhis2.mobile.commons.resources.formatting_error
import org.dhis2.mobile.commons.resources.invalid_email
import org.dhis2.mobile.commons.resources.invalid_field
import org.dhis2.mobile.commons.resources.invalid_integer
import org.dhis2.mobile.commons.resources.invalid_interval
import org.dhis2.mobile.commons.resources.invalid_negative_number
import org.dhis2.mobile.commons.resources.invalid_percentage
import org.dhis2.mobile.commons.resources.invalid_phone_number
import org.dhis2.mobile.commons.resources.invalid_possitive
import org.dhis2.mobile.commons.resources.invalid_possitive_zero
import org.dhis2.mobile.commons.resources.leading_zero_error
import org.dhis2.mobile.commons.resources.pattern_error
import org.dhis2.mobile.commons.resources.required
import org.dhis2.mobile.commons.resources.validation_error_message
import org.dhis2.mobile.commons.resources.validation_url
import org.dhis2.mobile.commons.resources.wrong_pattern
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
import org.jetbrains.compose.resources.getString

actual class FieldErrorMessageProvider {
    actual suspend fun getFriendlyErrorMessage(error: Throwable) = getString(parseErrorToMessageResource(error))

    private fun parseErrorToMessageResource(error: Throwable) =
        when (error) {
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
            is CustomIntentFailure -> getCustomIntentError(error)
            else -> Res.string.invalid_field
        }

    private fun getTrueOnlyError(error: TrueOnlyFailure) =
        when (error) {
            TrueOnlyFailure.BooleanMalformedException -> Res.string.error_true_only_malformed
            TrueOnlyFailure.FalseIsNotAValidValueException ->
                Res.string.error_true_only_false_not_valid

            TrueOnlyFailure.OneIsNotTrueException -> Res.string.error_true_only_one_is_not_true
        }

    private fun getTextError(error: TextFailure) =
        when (error) {
            TextFailure.TooLargeTextException -> Res.string.error_text_too_long
        }

    private fun getLetterError(error: LetterFailure) =
        when (error) {
            LetterFailure.EmptyStringException -> Res.string.error_letter_empty
            LetterFailure.MoreThanOneLetterException -> Res.string.error_letter_more_than_one
            LetterFailure.StringIsNotALetterException -> Res.string.error_letter_not_a_letter
        }

    private fun getTimeError(error: TimeFailure) =
        when (error) {
            TimeFailure.ParseException -> Res.string.error_time_parsing
        }

    private fun getDateTimeError(error: DateTimeFailure) =
        when (error) {
            DateTimeFailure.ParseException -> Res.string.error_date_time_parsing
        }

    private fun getDateError(error: DateFailure) =
        when (error) {
            DateFailure.ParseException -> Res.string.error_date_parsing
        }

    private fun getBooleanError(error: BooleanFailure) =
        when (error) {
            BooleanFailure.BooleanMalformedException -> Res.string.error_boolean_malformed
            BooleanFailure.OneIsNotTrueException -> Res.string.error_boolean_one_is_not_true
            BooleanFailure.ZeroIsNotFalseException -> Res.string.error_boolean_zero_is_not_false
        }

    private fun getCoordinateError(error: CoordinateFailure) =
        when (error) {
            CoordinateFailure.CoordinateMalformedException -> Res.string.wrong_pattern
        }

    private fun getFieldMaskError(error: FieldMaskFailure) =
        when (error) {
            FieldMaskFailure.WrongPatternException -> Res.string.wrong_pattern
            FieldMaskFailure.InvalidPatternException -> Res.string.pattern_error
        }

    private fun getPhoneNumberError(error: PhoneNumberFailure) =
        when (error) {
            PhoneNumberFailure.MalformedPhoneNumberException -> Res.string.invalid_phone_number
        }

    private fun getEmailError(error: EmailFailure) =
        when (error) {
            EmailFailure.MalformedEmailException -> Res.string.invalid_email
        }

    private fun getIntegerNegativeError(error: IntegerNegativeFailure) =
        when (error) {
            IntegerNegativeFailure.IntegerOverflow -> Res.string.formatting_error
            IntegerNegativeFailure.NumberFormatException -> Res.string.formatting_error
            IntegerNegativeFailure.ValueIsPositive -> Res.string.invalid_negative_number
            IntegerNegativeFailure.ValueIsZero -> Res.string.invalid_negative_number
            IntegerNegativeFailure.LeadingZeroException -> Res.string.leading_zero_error
        }

    private fun getIntegerZeroOrPositiveError(error: IntegerZeroOrPositiveFailure) =
        when (error) {
            IntegerZeroOrPositiveFailure.IntegerOverflow -> Res.string.formatting_error
            IntegerZeroOrPositiveFailure.NumberFormatException -> Res.string.formatting_error
            IntegerZeroOrPositiveFailure.ValueIsNegative -> Res.string.invalid_possitive_zero
            IntegerZeroOrPositiveFailure.LeadingZeroException -> Res.string.leading_zero_error
        }

    private fun getIntegerPositiveError(error: IntegerPositiveFailure) =
        when (error) {
            IntegerPositiveFailure.IntegerOverflow -> Res.string.formatting_error
            IntegerPositiveFailure.NumberFormatException -> Res.string.formatting_error
            IntegerPositiveFailure.ValueIsNegative -> Res.string.invalid_possitive
            IntegerPositiveFailure.ValueIsZero -> Res.string.invalid_possitive
            IntegerPositiveFailure.LeadingZeroException -> Res.string.leading_zero_error
        }

    private fun getUnitIntervalFailure(error: UnitIntervalFailure) =
        when (error) {
            UnitIntervalFailure.GreaterThanOneException -> Res.string.invalid_interval
            UnitIntervalFailure.NumberFormatException -> Res.string.formatting_error
            UnitIntervalFailure.ScientificNotationException -> Res.string.formatting_error
            UnitIntervalFailure.SmallerThanZeroException -> Res.string.invalid_interval
        }

    private fun getPercentageError(error: PercentageFailure) =
        when (error) {
            PercentageFailure.NumberFormatException -> Res.string.formatting_error
            PercentageFailure.ValueGreaterThan100 -> Res.string.invalid_percentage
            PercentageFailure.ValueIsNegative -> Res.string.invalid_possitive
        }

    private fun getUrlError(error: UrlFailure) =
        when (error) {
            UrlFailure.MalformedUrlException -> Res.string.validation_url
        }

    private fun getIntegerError(error: IntegerFailure) =
        when (error) {
            IntegerFailure.IntegerOverflow -> Res.string.formatting_error
            IntegerFailure.NumberFormatException -> Res.string.invalid_integer
            IntegerFailure.LeadingZeroException -> Res.string.leading_zero_error
        }

    private fun getCustomIntentError(error: CustomIntentFailure) =
        when (error) {
            CustomIntentFailure.CouldNotRetrieveCustomIntentData -> Res.string.custom_intent_error
        }

    private fun getNumberError(error: NumberFailure) =
        when (error) {
            NumberFailure.NumberFormatException -> Res.string.formatting_error
            NumberFailure.ScientificNotationException -> Res.string.formatting_error
            NumberFailure.LeadingZeroException -> Res.string.leading_zero_error
        }

    suspend fun mandatoryWarning(): String = getString(Res.string.required)

    suspend fun defaultValidationErrorMessage(): String = getString(Res.string.validation_error_message)
}

// To be improved and refactored in ANDROAPP-7268
sealed class CustomIntentFailure : Throwable() {
    object CouldNotRetrieveCustomIntentData : CustomIntentFailure() {
        private fun readResolve(): Any = CouldNotRetrieveCustomIntentData
    }
}
