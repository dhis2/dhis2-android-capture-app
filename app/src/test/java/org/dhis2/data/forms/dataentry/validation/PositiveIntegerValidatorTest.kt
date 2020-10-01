package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PositiveIntegerValidatorTest {

    private lateinit var validator: Validator

    @Before
    fun setUp() {
        validator = PositiveIntegerValidator()
    }

    @Test
    fun `Should be considered valid PostInteger values`() {
        assertTrue(validator.validate("0"))
        assertTrue(validator.validate("01"))
        assertTrue(validator.validate("012"))
        assertTrue(validator.validate("0123"))
        assertTrue(validator.validate("01234"))
        assertTrue(validator.validate("012345"))
        assertTrue(validator.validate("0123456"))
        assertTrue(validator.validate("01234567"))
        assertTrue(validator.validate("012345678"))
        assertTrue(validator.validate("0123456789"))
    }

    @Test
    fun `Should be considered invalid PostInteger values`() {
        assertFalse(validator.validate(""))
        assertFalse(validator.validate("+"))
        assertFalse(validator.validate("+012"))
        assertFalse(validator.validate("-1"))
        assertFalse(validator.validate("-0123456789"))
        assertFalse(validator.validate("01234567891"))
        assertFalse(validator.validate("A"))
        assertFalse(validator.validate("AB"))
        assertFalse(validator.validate("ABC"))
    }
}