package org.dhis2.uicomponents.map.geometry.point

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class PointViewModel : ViewModel() {

    var layer: SymbolLayer? = null
    var source: GeoJsonSource? = null
    val lat = ObservableField<String>()
    val lng = ObservableField<String>()

    fun setPoint(p: Point) {
        lat.set(p.latitude().toString())
        lng.set(p.longitude().toString())
    }

    fun getPointAsString(): String? {
        return try {
            val list = mutableListOf<Double>()
            list.add(lng.get()!!.toDouble())
            list.add(lat.get()!!.toDouble())
            Gson().toJson(list)
        } catch (e: Exception) {
            null
        }
    }

    fun getId(): String {
        return "point_marker_id"
    }
}
