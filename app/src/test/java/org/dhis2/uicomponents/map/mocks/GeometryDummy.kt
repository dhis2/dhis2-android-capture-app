package org.dhis2.uicomponents.map.mocks

import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

object GeometryDummy {
    fun getGeometryAsPointFrom(): Geometry =
        Geometry
            .builder()
            .type(FeatureType.POINT)
            .coordinates(FROM_COORDINATES)
            .build()

    fun getGeometryAsPointTo(): Geometry =
        Geometry
            .builder()
            .type(FeatureType.POINT)
            .coordinates(TO_COORDINATES)
            .build()

    fun getGeometryAsPointWrong(): Geometry =
        Geometry
            .builder()
            .type(FeatureType.POINT)
            .coordinates(WRONG_COORDINATES)
            .build()

    fun getGeometryAsPoint(coordinates: String): Geometry =
        Geometry
            .builder()
            .type(FeatureType.POINT)
            .coordinates(coordinates)
            .build()

    const val FROM_COORDINATES = "[-30.00, 11.00]"
    const val TO_COORDINATES = "[-35.00, 15.00]"
    private const val WRONG_COORDINATES = "[-181.00, 11.00]"
}
