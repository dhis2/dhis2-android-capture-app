package org.dhis2.uicomponents.map.geometry.line

import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point

class MapLineToFeature {

    fun map(line: List<Point>) : Feature {
        val lineString = LineString.fromLngLats(line)
        return Feature.fromGeometry(lineString)
    }
}
