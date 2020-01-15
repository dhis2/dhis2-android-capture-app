package org.dhis2.usescases.map

import org.dhis2.usescases.map.point.PointViewModel
import org.junit.Before
import org.junit.Test
import com.mapbox.geojson.Point

class PointViewModelTest {

    private lateinit var pointViewModel: PointViewModel

    @Before
    fun setup() {
        pointViewModel = PointViewModel()
    }

    @Test
    fun `Should set point`() {
        pointViewModel.setPoint(Point.fromLngLat(0.1, 0.0))
        assert(pointViewModel.lat.get() == "0.0" && pointViewModel.lng.get() == "0.1")
    }

    @Test
    fun `Should get point as string where point is defined`() {
        pointViewModel.setPoint(Point.fromLngLat(0.1, 0.0))
        assert(pointViewModel.getPointAsString() == "[0.1,0.0]")
    }

    @Test
    fun `Should get point as string where point is null`() {
        assert(pointViewModel.getPointAsString() == null)
    }

}