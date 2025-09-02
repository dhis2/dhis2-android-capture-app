package org.dhis2.form.data

import org.hisp.dhis.android.core.common.FeatureType
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GeometryControllerTest {
    private val geometryParser: GeometryParser = mock()
    private lateinit var controller: GeometryController

    @Before
    fun setUp() {
        controller = GeometryController(geometryParser)
    }

    @Test
    fun `Should return null if coordinates is null`() {
        assertTrue(
            controller.generateLocationFromCoordinates(FeatureType.POINT, null) == null,
        )
    }

    @Test
    fun `Should parse coordinates`() {
        val point = listOf(12.0, 12.0)
        whenever(
            geometryParser.parsePoint(any()),
        ) doReturn point

        val result =
            controller.generateLocationFromCoordinates(
                FeatureType.POINT,
                "coordinates",
            )

        assertTrue(
            result?.type() == FeatureType.POINT &&
                result.coordinates()?.isNotEmpty() == true,
        )
    }
}
