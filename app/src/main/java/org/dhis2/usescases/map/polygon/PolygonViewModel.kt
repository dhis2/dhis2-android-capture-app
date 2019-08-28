package org.dhis2.usescases.map.polygon

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.*

class PolygonViewModel(val app: Application): AndroidViewModel(app) {

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

    fun getPointAsString(): String? {
        val list = mutableListOf<MutableList<MutableList<Double>>>()
        list.add(mutableListOf())
        _response.value?.forEach {
            it.point?.let {point ->
                list[0].add(mutableListOf(point.latitude(), point.longitude()))
            }
        }
        return if (list[0].size > 3) {
            Gson().toJson(list)
        } else {
            Toast.makeText(app, "Polygon must contains at least 4 points.", Toast.LENGTH_SHORT).show()
            null
        }
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