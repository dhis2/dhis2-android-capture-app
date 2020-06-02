package org.dhis2.uicomponents.map.geometry.bound

import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class GetBoundingBoxTest {

    private val getBoundingBox = GetBoundingBox()

    @Test
    fun `Should get bounding box`() {
        val coordinates = prepareCoordinates()

        val result = getBoundingBox.getEnclosingBoundingBox(coordinates)
        val limitSouthWest = result.southwest()
        val limitNorthEast = result.northeast()

        val belowLongitudeWithPadding = -68.56257500000001
        val belowLatitudeWithPadding = -54.377759
        val mediumLongitudeWithPadding = -1.7976931348623157E308
        val upperLatitudeWithPadding = 72.511722

        assertThat(limitSouthWest.longitude(), `is`(belowLongitudeWithPadding))
        assertThat(limitSouthWest.latitude(), `is`(belowLatitudeWithPadding))
        assertThat(limitNorthEast.longitude(), `is`(mediumLongitudeWithPadding))
        assertThat(limitNorthEast.latitude(), `is`(upperLatitudeWithPadding))
    }

    private fun prepareCoordinates(): List<LatLng> {
        val below = LatLng(-54.367759,-68.552575)
        val upper = LatLng(72.501722, 52.131954)
        val medium = LatLng(20.632784,-0.285835)
        return listOf(below, upper, medium)
    }
}
