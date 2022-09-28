package org.dhis2.uicomponents.map.geometry.bound

import com.mapbox.geojson.Point
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class GetBoundingBoxTest {

    private val getBoundingBox = org.dhis2.maps.geometry.bound.GetBoundingBox()

    @Test
    fun `Should get bounding box`() {
        val coordinates = prepareCoordinates()

        val result = getBoundingBox.getEnclosingBoundingBox(coordinates)
        val limitSouthWest = result.southwest()
        val limitNorthEast = result.northeast()

        val belowLongitudeWithPadding = -68.56257500000001
        val belowLatitudeWithPadding = -54.377759
        val mediumLongitudeWithPadding = 52.141954
        val upperLatitudeWithPadding = 72.511722

        assertThat(limitSouthWest.longitude(), `is`(belowLongitudeWithPadding))
        assertThat(limitSouthWest.latitude(), `is`(belowLatitudeWithPadding))
        assertThat(limitNorthEast.longitude(), `is`(mediumLongitudeWithPadding))
        assertThat(limitNorthEast.latitude(), `is`(upperLatitudeWithPadding))
    }

    private fun prepareCoordinates(): List<Point> {
        val below = Point.fromLngLat(-68.552575, -54.367759)
        val upper = Point.fromLngLat(52.131954, 72.501722)
        val medium = Point.fromLngLat(-0.285835, 20.632784)
        return listOf(below, upper, medium)
    }
}
