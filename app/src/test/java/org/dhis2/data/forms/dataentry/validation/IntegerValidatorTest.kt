package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IntegerValidatorTest {

    private lateinit var validator: Validator

    @Before
    fun setUp() {
        validator = IntegerValidator()
    }

    @Test
    fun `Should be considered valid Integer values`() {
        assertTrue(validator.validate("1"))
        assertTrue(validator.validate("10"))
        assertTrue(validator.validate("01"))
        assertTrue(validator.validate("012"))
        assertTrue(validator.validate("0123"))
        assertTrue(validator.validate("01234"))
        assertTrue(validator.validate("012345"))
        assertTrue(validator.validate("0123456"))
        assertTrue(validator.validate("01234567"))
        assertTrue(validator.validate("012345678"))
        assertTrue(validator.validate("0123456789"))
        assertTrue(validator.validate("2147483647"))
        assertTrue(validator.validate("0"))
        assertTrue(validator.validate("-0123456789"))
        assertTrue(validator.validate("-1"))
        assertTrue(validator.validate("-01"))
        assertTrue(validator.validate("-012"))
        assertTrue(validator.validate("-0123"))
        assertTrue(validator.validate("-01234"))
        assertTrue(validator.validate("-012345"))
        assertTrue(validator.validate("-0123456"))
        assertTrue(validator.validate("-01234567"))
        assertTrue(validator.validate("-012345678"))
        assertTrue(validator.validate("-0123456789"))
        assertTrue(validator.validate("-01234567891"))
        assertTrue(validator.validate("-999999999"))
        assertTrue(validator.validate("-2147483648"))
    }

    @Test
    fun `Should be considered invalid Integer values`() {
        assertFalse(validator.validate(""))
        assertFalse(validator.validate("A012"))
        assertFalse(validator.validate("9999999999"))
        assertFalse(validator.validate("2147483648"))
        assertFalse(validator.validate("A"))
        assertFalse(validator.validate("AB"))
        assertFalse(validator.validate("ABC"))
        assertFalse(validator.validate("0ABC"))
        assertFalse(validator.validate("1ABC2"))
    }
}
