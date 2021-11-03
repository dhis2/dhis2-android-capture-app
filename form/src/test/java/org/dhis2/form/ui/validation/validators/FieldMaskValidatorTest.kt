package org.dhis2.form.ui.validation.validators

import org.dhis2.form.ui.validation.failures.FieldMaskFailure
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldMaskValidatorTest {

    @Test
    fun `should validate text fieldmask`() {
        Assert.assertFalse(FieldMaskValidator("\\w\\w\\x\\x").fieldMaskIsCorrect())
        assertTrue(FieldMaskValidator("\'w\'w\'x\'x").fieldMaskIsCorrect())
    }

    @Test
    fun `should call onSuccess if valueToEvaluate is null`() {
        FieldMaskValidator("\'w\'w\'x\'x").validateNullSafe(null).fold(
            { assertTrue(true) },
            { assertTrue(false) }
        )
    }

    @Test
    fun `should call onSuccess if valueToEvaluate is empty`() {
        FieldMaskValidator("\'w\'w\'x\'x").validate("").fold(
            { assertTrue(true) },
            { assertTrue(false) }
        )
    }

    @Test
    fun `should call onSuccess if fieldMask is null`() {
        FieldMaskValidator(null).validate("hello").fold(
            { assertTrue(true) },
            { assertTrue(false) }
        )
    }

    @Test
    fun `should call onPatterError if fieldMask is wrong`() {
        FieldMaskValidator("\\w\\w\\x\\x").validate(
            "hello"
        ).fold(
            { assertTrue(false) },
            { assertTrue(it is FieldMaskFailure.InvalidPatternException) }
        )
    }

    @Test
    fun `should call onSuccess if value matches pattern`() {
        FieldMaskValidator("\\w\\d").validate(
            "A1"
        ).fold(
            { assertTrue(true) },
            { assertTrue(false) }
        )
    }

    @Test
    fun `should call onError if value does not matches pattern`() {
        FieldMaskValidator("\\w\\d").validate(
            "AA1"
        ).fold(
            { assertTrue(false) },
            { assertTrue(it is FieldMaskFailure.WrongPatternException) }
        )
    }
}
