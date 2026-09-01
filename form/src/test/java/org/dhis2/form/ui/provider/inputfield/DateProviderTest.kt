package org.dhis2.form.ui.provider.inputfield

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Test

class DateProviderTest {
    @Test
    fun `should accept a complete date`() {
        assertEquals(true, checkValueLengthWithTypeIsValid(10, ValueType.DATE))
        assertEquals(false, checkValueLengthWithTypeIsValid(9, ValueType.DATE))
    }

    @Test
    fun `should accept a complete date time`() {
        assertEquals(true, checkValueLengthWithTypeIsValid(16, ValueType.DATETIME))
        assertEquals(false, checkValueLengthWithTypeIsValid(10, ValueType.DATETIME))
    }

    @Test
    fun `should accept a complete time`() {
        assertEquals(true, checkValueLengthWithTypeIsValid(5, ValueType.TIME))
        assertEquals(false, checkValueLengthWithTypeIsValid(4, ValueType.TIME))
    }

    @Test
    fun `should fall back to the date length for any other value type`() {
        assertEquals(true, checkValueLengthWithTypeIsValid(10, ValueType.AGE))
        assertEquals(true, checkValueLengthWithTypeIsValid(10, null))
        assertEquals(false, checkValueLengthWithTypeIsValid(5, null))
    }
}
