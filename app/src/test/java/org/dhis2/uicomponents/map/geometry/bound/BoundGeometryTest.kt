package org.dhis2.uicomponents.map.geometry.bound

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BoundGeometryTest {

    private lateinit var boundsGeometry: BoundsGeometry

    @Test
    fun `Should init or reset`() {
        boundsGeometry = BoundsGeometry()

        boundsGeometry.initOrReset()
        assertThat(boundsGeometry.eastBound, `is`(0.0))
        assertThat(boundsGeometry.northBound, `is`(0.0))
        assertThat(boundsGeometry.southBound, `is`(0.0))
        assertThat(boundsGeometry.westBound, `is`(0.0))
    }

    @Test
    fun `Should update first time`() {
        val latitude = 40.979898
        val longitude = -4.155605
        boundsGeometry = BoundsGeometry()

        boundsGeometry.update(latitude, longitude)

        assertThat(boundsGeometry.northBound, `is`(latitude))
        assertThat(boundsGeometry.southBound, `is`(latitude))

        assertThat(boundsGeometry.eastBound, `is`(longitude))
        assertThat(boundsGeometry.westBound, `is`(longitude))
    }

    @Test
    fun `Should update other times`() {
        val latitude = 40.979898
        val longitude = -4.155605
        val newLatitude = 39.979898
        val newLongitude = -3.155605

        boundsGeometry = BoundsGeometry(latitude, latitude, longitude, longitude)
        boundsGeometry.update(newLatitude, newLongitude)

        assertThat(boundsGeometry.northBound, `is`(latitude))
        assertThat(boundsGeometry.southBound, `is`(newLatitude))

        assertThat(boundsGeometry.eastBound, `is`(newLongitude))
        assertThat(boundsGeometry.westBound, `is`(longitude))
    }
}
