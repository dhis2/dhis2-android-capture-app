package org.dhis2.maps.geometry.point

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Point

class PointViewModel : ViewModel() {
    var layer: SymbolLayer? = null
    var source: GeoJsonSource? = null
    val lat = ObservableField<String>()
    val lng = ObservableField<String>()

    fun setPoint(p: Point) {
        lat.set(p.latitude().toString())
        lng.set(p.longitude().toString())
    }

    fun getPointAsString(): String? =
        try {
            val list = mutableListOf<Double>()
            list.add(lng.get()!!.toDouble())
            list.add(lat.get()!!.toDouble())
            Gson().toJson(list)
        } catch (e: Exception) {
            null
        }

    fun getId(): String = "point_marker_id"
}
