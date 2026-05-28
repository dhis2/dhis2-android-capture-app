package org.dhis2.maps.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

class CoordinateUtilsTest {

    private val gson = Gson()
    private val ringType = object : TypeToken<List<List<List<Double>>>>() {}.type

    @Test
    fun `geometryCoordinates returns null when geometry is null`() {
        assertNull(CoordinateUtils.geometryCoordinates(null))
    }

    @Test
    fun `geometryCoordinates returns truncated JSON coordinates for Point`() {
        val point = Point.fromLngLat(10.123456789, 20.987654321)

        val result = CoordinateUtils.geometryCoordinates(point)

        val coordinates: List<Double> = gson.fromJson(
            result,
            object : TypeToken<List<Double>>() {}.type,
        )
        assertEquals(10.12345, coordinates[0], 0.0)
        assertEquals(20.98765, coordinates[1], 0.0)
    }

    @Test
    fun `geometryCoordinates closes open polygon ring when first and last coordinates differ`() {
        val pointA = Point.fromLngLat(0.0, 0.0)
        val pointB = Point.fromLngLat(1.0, 0.0)
        val pointC = Point.fromLngLat(0.0, 1.0)
        val polygon = Polygon.fromLngLats(listOf(listOf(pointA, pointB, pointC)))

        val result = CoordinateUtils.geometryCoordinates(polygon)

        val rings: List<List<List<Double>>> = gson.fromJson(result, ringType)
        val ring = rings[0]
        assertEquals(
            "Ring should start and end with the same coordinates",
            ring.first(),
            ring.last()
        )
    }

    @Test
    fun `geometryCoordinates does not duplicate closing coordinate when polygon ring is already closed`() {
        val pointA = Point.fromLngLat(0.0, 0.0)
        val pointB = Point.fromLngLat(1.0, 0.0)
        val pointC = Point.fromLngLat(0.0, 1.0)
        val polygon = Polygon.fromLngLats(listOf(listOf(pointA, pointB, pointC, pointA)))

        val result = CoordinateUtils.geometryCoordinates(polygon)

        val rings: List<List<List<Double>>> = gson.fromJson(result, ringType)
        val ring = rings[0]
        assertEquals(
            "Ring should start and end with the same coordinates",
            ring.first(),
            ring.last()
        )
        assertEquals("Closing coordinate must not be duplicated", 4, ring.size)
    }
}
