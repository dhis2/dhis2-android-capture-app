package org.dhis2.extensions

import org.dhis2.composetable.ui.extensions.isNumeric
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun shouldReturnTrueIfTextIsNumericFalseOtherwise() {
        assertTrue("0".isNumeric())
        assertTrue("231".isNumeric())
        assertTrue("11.33".isNumeric())
        assertTrue("-123.3".isNumeric())
        assertTrue("11232.54".isNumeric())
        assertFalse("1,1232.54".isNumeric())
        assertFalse("One".isNumeric())
        assertFalse("[89.23, -14.20]".isNumeric())
        assertFalse("12/01/2020".isNumeric())
        assertFalse("has 1.2".isNumeric())
    }
}
