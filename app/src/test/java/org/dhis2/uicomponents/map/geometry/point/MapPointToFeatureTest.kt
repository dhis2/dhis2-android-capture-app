package org.dhis2.uicomponents.map.geometry.point

import org.dhis2.maps.geometry.bound.BoundsGeometry
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapPointToFeatureTest {

    private lateinit var mapPointToFeature: MapPointToFeature
    private val boundsGeometry: BoundsGeometry = mock()

    @Before
    fun setup() {
        mapPointToFeature = MapPointToFeature()
    }

    @Test
    fun `Should map point to feature`() {
        val latitude = 11.00
        val longitude = -30.00
        val geometry = Geometry.builder()
            .coordinates("[-30.00, 11.00]")
            .type(FeatureType.POINT)
            .build()

        whenever(boundsGeometry.update(latitude, longitude)) doReturn BoundsGeometry(
            latitude,
            latitude,
            longitude,
            longitude,
        )

        val result = mapPointToFeature.map(geometry, boundsGeometry)

        val expectedResult = "{\"type\":\"Point\",\"coordinates\":[-30.0,11.0]}"
        assertThat(result?.first?.geometry()?.toJson(), `is`(expectedResult))
        result?.second?.let {
            assertThat(it.northBound, `is`(latitude))
            assertThat(it.southBound, `is`(latitude))
            assertThat(it.eastBound, `is`(longitude))
            assertThat(it.westBound, `is`(longitude))
        }
    }

    @Test
    fun `Should not map point to feature`() {
        val geometry = Geometry.builder()
            .coordinates("[-181.00, 11.00]")
            .type(FeatureType.POINT)
            .build()

        val result = mapPointToFeature.map(geometry, boundsGeometry)

        assertEquals(result, null)
    }
}
