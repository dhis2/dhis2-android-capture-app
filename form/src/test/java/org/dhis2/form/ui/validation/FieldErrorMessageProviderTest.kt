package org.dhis2.form.ui.validation

import android.content.Context
import org.dhis2.form.R
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerNegativeFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerPositiveFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure
import org.hisp.dhis.android.core.common.valuetype.validation.failures.NumberFailure
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FieldErrorMessageProviderTest {

    private val context: Context = mock()
    private lateinit var fieldErrorMessageProvider: FieldErrorMessageProvider

    @Before
    fun setUp() {
        whenever(context.getString(R.string.leading_zero_error)) doReturn LEADING_ZERO_MESSAGE

        fieldErrorMessageProvider = FieldErrorMessageProvider(context)
    }

    @Test
    fun `validation throws an integer negative number with leading zero exception`() {
        // Given validation throws an integer negative with leading zero exception
        val error = IntegerNegativeFailure.LeadingZeroException

        // When is generation friendly error message
        fieldErrorMessageProvider.getFriendlyErrorMessage(error)

        // Then it is providing the correct message
        assertEquals(context.getString(R.string.leading_zero_error), LEADING_ZERO_MESSAGE)
    }

    @Test
    fun `validation throws an integer zero or positive with leading zero exception`() {
        // Given validation throws an integer zero or positive with leading zero exception
        val error = IntegerZeroOrPositiveFailure.LeadingZeroException

        // When is generation friendly error message
        fieldErrorMessageProvider.getFriendlyErrorMessage(error)

        // Then it is providing the correct message
        assertEquals(context.getString(R.string.leading_zero_error), LEADING_ZERO_MESSAGE)
    }

    @Test
    fun `validation throws an integer positive with leading zero exception`() {
        // Given validation throws an integer positive with leading zero exception
        val error = IntegerPositiveFailure.LeadingZeroException

        // When is generation friendly error message
        fieldErrorMessageProvider.getFriendlyErrorMessage(error)

        // Then it is providing the correct message
        assertEquals(context.getString(R.string.leading_zero_error), LEADING_ZERO_MESSAGE)
    }

    @Test
    fun `validation throws an integer with leading zero exception`() {
        // Given validation throws an integer with leading zero exception
        val error = IntegerFailure.LeadingZeroException

        // When is generation friendly error message
        fieldErrorMessageProvider.getFriendlyErrorMessage(error)

        // Then it is providing the correct message
        assertEquals(context.getString(R.string.leading_zero_error), LEADING_ZERO_MESSAGE)
    }

    @Test
    fun `validation throws an number with leading zero exception`() {
        // Given validation throws an number with leading zero exception
        val error = NumberFailure.LeadingZeroException

        // When is generation friendly error message
        fieldErrorMessageProvider.getFriendlyErrorMessage(error)

        // Then it is providing the correct message
        assertEquals(context.getString(R.string.leading_zero_error), LEADING_ZERO_MESSAGE)
    }

    companion object {
        const val LEADING_ZERO_MESSAGE = "Leading zero numbers are not allowed"
    }
}
