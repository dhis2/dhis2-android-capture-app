package org.dhis2.usescases.map.point

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class PointViewModel(app: Application): AndroidViewModel(app) {

    var layer: SymbolLayer? = null
    var source: GeoJsonSource? = null
    val lat = ObservableField<String>()
    val lng = ObservableField<String>()

    fun setPoint(p: Point) {
        lat.set(p.latitude().toString())
        lng.set(p.longitude().toString())
    }

    fun getPointAsString() {

    }

    fun getId(): String {
        return "point_marker_id"
    }
}