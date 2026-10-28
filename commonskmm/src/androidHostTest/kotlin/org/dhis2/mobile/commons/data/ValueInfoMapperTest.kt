package org.dhis2.mobile.commons.data

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValueInfoMapperTest {
    private val valueParser: ValueParser =
        mock {
            onBlocking { valueAsOptionExists(any(), any()) } doReturn false
            onBlocking { valueAsOrgUnitExists(any()) } doReturn false
            onBlocking { valueAsFileExists(any()) } doReturn false
        }

    @Test
    fun shouldDescribeDateFieldWithoutLookingUpOptions() =
        runTest {
            val valueInfo = valueParser.getValueInfo(ValueType.DATE, null, "2024-04-28")

            assertTrue(valueInfo.isDate)
            assertFalse(valueInfo.parseToOptionName())
            verify(valueParser, never()).valueAsOptionExists(any(), any())
        }

    @Test
    fun shouldDescribeOrgUnitFieldWithAnExistingOrgUnit() =
        runTest {
            whenever(valueParser.valueAsOrgUnitExists("orgUnitUid")) doReturn true

            val valueInfo =
                valueParser.getValueInfo(ValueType.ORGANISATION_UNIT, null, "orgUnitUid")

            assertTrue(valueInfo.parseToOrgUnitName())
        }

    @Test
    fun shouldDescribeCoordinateField() =
        runTest {
            val valueInfo = valueParser.getValueInfo(ValueType.COORDINATE, null, "[1.0, 2.0]")

            assertTrue(valueInfo.isCoordinate)
        }

    @Test
    fun shouldDescribeOptionSetFieldWithAnExistingOption() =
        runTest {
            whenever(valueParser.valueAsOptionExists("optionSetUid", "optionCode")) doReturn true

            val valueInfo = valueParser.getValueInfo(ValueType.TEXT, "optionSetUid", "optionCode")

            assertEquals("optionSetUid", valueInfo.optionSetUid)
            assertTrue(valueInfo.parseToOptionName())
        }
}
