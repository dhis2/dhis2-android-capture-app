package org.dhis2.bindings

import org.dhis2.Bindings.withValueTypeCheck
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertTrue
import org.junit.Test

class ValueExtensionsTest {
    @Test
    fun `Should parse to correct integer format`() {
        val valueList: List<String?> = arrayListOf(
            "",
            "0.0",
            "1.2",
            "1",
            "1234",
            null
        )

        val expectedResults = arrayListOf(
            "",
            "0",
            "1",
            "1",
            "1234",
            null
        )

        valueList.forEachIndexed { index, value ->
            assertTrue(
                value.withValueTypeCheck(ValueType.INTEGER) == expectedResults[index]
            )
        }
    }
}
