package org.dhis2.uicomponents.map.geometry.polygon

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.geojson.Point
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PolygonViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var polygonViewModel: org.dhis2.maps.geometry.polygon.PolygonViewModel

    @Before
    fun setup() {
        polygonViewModel = org.dhis2.maps.geometry.polygon.PolygonViewModel()
        polygonViewModel.onMessage = {}
    }

    @Test
    fun `Should add point with point`() {
        val point = polygonViewModel.createPolygonPoint()
        point.point = Point.fromLngLat(0.1, 0.0)
        polygonViewModel.add(point)
        assert(polygonViewModel.response.value?.size == 1)
    }

    @Test
    fun `Should add point without point`() {
        polygonViewModel.add(polygonViewModel.createPolygonPoint())
        assert(polygonViewModel.response.value?.size == 0)
    }

    @Test
    fun `Should remove point of live data`() {
        val point = polygonViewModel.createPolygonPoint().apply {
            point = Point.fromLngLat(0.1, 0.0)
        }
        polygonViewModel.add(point)
        polygonViewModel.remove(point)
        assert(polygonViewModel.response.value?.size == 0)
    }

    @Test
    fun `Should check points as string`() {
        val point1 = polygonViewModel.createPolygonPoint().apply {
            point = Point.fromLngLat(0.1, 0.0)
        }
        val point2 = polygonViewModel.createPolygonPoint().apply {
            point = Point.fromLngLat(0.1, 0.1)
        }
        val point3 = polygonViewModel.createPolygonPoint().apply {
            point = Point.fromLngLat(0.0, 0.1)
        }
        polygonViewModel.response.observeForever {}
        polygonViewModel.add(point1)
        polygonViewModel.add(point2)
        polygonViewModel.add(point3)
        assert(polygonViewModel.getPointAsString() == "[[[0.1,0.0],[0.1,0.1],[0.0,0.1]]]")
    }

    @Test
    fun `Should check points as string without points`() {
        polygonViewModel.response.observeForever {}
        assert(polygonViewModel.getPointAsString() == null)
    }
}
