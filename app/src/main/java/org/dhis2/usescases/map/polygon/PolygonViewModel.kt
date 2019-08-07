package org.dhis2.usescases.map.polygon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.*

class PolygonViewModel(app: Application): AndroidViewModel(app) {

    private var _response = MutableLiveData<MutableList<PolygonPoint>>()
    val response: LiveData<MutableList<PolygonPoint>>
        get() = _response

    init {
        _response.value = mutableListOf()
    }

    fun add(polygonPoint: PolygonPoint) {
        if (polygonPoint.point != null) {
            val list = _response.value
            list?.add(polygonPoint)
            _response.value = list
        }
    }


    fun remove(polygonPoint: PolygonPoint) {
        val list = _response.value
        list?.remove(polygonPoint)
        _response.value = list
    }

    fun createPolygonPoint(): PolygonPoint
    {
        return PolygonPoint()
    }

    inner class PolygonPoint(var point: Point? = null, var source: GeoJsonSource? = null, var layer: SymbolLayer? = null, var selected: Boolean = true) {
        val uuid = UUID.randomUUID().toString()
        override fun toString(): String {
            point?.let {
                return "${it.latitude().toString().take(8)}, ${it.longitude().toString().take(8)}"
            }
            return ""
        }
    }
}