package org.dhis2.usescases.map

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.geojson.Point
import org.dhis2.usescases.map.polygon.PolygonViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PolygonViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var polygonViewModel: PolygonViewModel

    @Before
    fun setup() {
        polygonViewModel = PolygonViewModel()
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
        val point = polygonViewModel.createPolygonPoint().apply {
            point = Point.fromLngLat(0.1, 0.0)
        }
        polygonViewModel.response.observeForever {}
        polygonViewModel.add(point)
        polygonViewModel.add(point)
        polygonViewModel.add(point)
        assert(polygonViewModel.getPointAsString() == "[[[0.1,0.0],[0.1,0.0],[0.1,0.0],[0.1,0.0]]]")
    }

    @Test
    fun `Should check points as string without points`() {
        polygonViewModel.response.observeForever {}
        assert(polygonViewModel.getPointAsString() == null)
    }

}