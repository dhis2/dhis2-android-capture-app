package org.dhis2.mobile.commons.data

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.D2
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class ValueParserImplTest {
    private val valueParser = ValueParserImpl(mock<D2>())

    @Test
    fun `should format point coordinates as latitude and longitude`() =
        runTest {
            val result = valueParser.valueFromCoordinateAsLatLong("[18.17778,49.31161]")

            assertEquals("Lat: 49.31161\nLong: 18.17778", result)
        }

    @Test
    fun `should return polygon coordinates unchanged`() =
        runTest {
            val polygon =
                "[[[-122.08972,37.42428],[-122.07844,37.4215],[-122.08395,37.40746],[-122.08972,37.42428]]]"

            val result = valueParser.valueFromCoordinateAsLatLong(polygon)

            assertEquals(polygon, result)
        }

    @Test
    fun `should return multipolygon coordinates unchanged`() =
        runTest {
            val multiPolygon =
                "[[[[18.17778,49.31161],[18.1725,49.31077],[18.17323,49.31502],[18.17778,49.31161]]]]"

            val result = valueParser.valueFromCoordinateAsLatLong(multiPolygon)

            assertEquals(multiPolygon, result)
        }
}
