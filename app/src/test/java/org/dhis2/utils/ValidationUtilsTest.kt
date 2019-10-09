package org.dhis2.utils

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `integer should remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.INTEGER, "0120")
        assertTrue(result == "120")
    }

    @Test
    fun `integer negative should remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.INTEGER_NEGATIVE, "-0120")
        assertTrue(result == "-120")
    }

    @Test
    fun `integer positive should remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.INTEGER_POSITIVE, "0120")
        assertTrue(result == "120")
    }

    @Test
    fun `integer zero or positive should remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.INTEGER_ZERO_OR_POSITIVE, "0120")
        assertTrue(result == "120")
    }

    @Test
    fun `number should have only one decimal and remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.NUMBER, "0120.123")
        assertTrue(result == "120.1")
    }

    @Test
    fun `percentage should remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.PERCENTAGE, "035")
        assertTrue(result == "35")
    }

    @Test
    fun `unit interval should remove left zeroes`() {
        val result = ValidationUtils.validate(ValueType.UNIT_INTERVAL, "000.01")
        assertTrue(result == "0.01")
    }

    @Test
    fun `null value should return null`() {
        val result = ValidationUtils.validate(ValueType.UNIT_INTERVAL, null)
        assertNull(result)
    }
}
