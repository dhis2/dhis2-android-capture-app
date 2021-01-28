package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NumberValidatorTest {

    private lateinit var validator: Validator

    @Before
    fun setUp() {
        validator = NumberValidator()
    }

    @Test
    fun `Should be considered valid Number values`() {
        assertTrue(validator.validate("1"))
        assertTrue(validator.validate("0123456789"))
        assertTrue(validator.validate("0"))
        assertTrue(validator.validate("-1"))
        assertTrue(validator.validate("-01234567891"))
        assertTrue(validator.validate("-999999999"))
        assertTrue(validator.validate("9999999999"))
        assertTrue(validator.validate("1.7976931348623157E308D"))
    }

    @Test
    fun `Should be considered invalid Number values`() {
        assertFalse(validator.validate(""))
        assertFalse(validator.validate("A012"))
        assertFalse(validator.validate("A"))
        assertFalse(validator.validate("AB"))
        assertFalse(validator.validate("ABC"))
        assertFalse(validator.validate("0ABC"))
        assertFalse(validator.validate("1ABC2"))
    }
}
