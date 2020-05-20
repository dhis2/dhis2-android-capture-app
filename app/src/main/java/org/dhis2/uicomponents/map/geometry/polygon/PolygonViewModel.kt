package org.dhis2.uicomponents.map.geometry.polygon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.UUID

class PolygonViewModel : ViewModel() {

    private var _response = MutableLiveData<MutableList<PolygonPoint>>()
    val response: LiveData<MutableList<PolygonPoint>>
        get() = _response

    lateinit var onMessage: (str: String) -> Unit

    init {
        _response.postValue(mutableListOf())
    }

    fun add(polygonPoint: PolygonPoint) {
        if (polygonPoint.point != null) {
            val list = _response.value
            list?.add(polygonPoint)
            _response.postValue(list)
        }
    }

    fun remove(polygonPoint: PolygonPoint) {
        val list = _response.value
        list?.remove(polygonPoint)
        _response.postValue(list)
    }

    fun createPolygonPoint(): PolygonPoint {
        return PolygonPoint()
    }

    fun getPointAsString(): String? {
        val list = mutableListOf<MutableList<MutableList<Double>>>()
        list.add(mutableListOf())
        _response.value?.forEach {
            it.point?.let { point ->
                list[0].add(mutableListOf(point.longitude(), point.latitude()))
            }
        }

        return if (list[0].size > 2) {
            list[0].add(list[0][0]) // set last point same as first
            Gson().toJson(list)
        } else {
            onMessage("Polygon must contains at least 4 points.")
            null
        }
    }

    inner class PolygonPoint(
        var point: Point? = null,
        var source: GeoJsonSource? = null,
        var layer: SymbolLayer? = null,
        var selected: Boolean = true
    ) {
        val uuid = UUID.randomUUID().toString()
        override fun toString(): String {
            point?.let {
                return "${it.longitude().toString().take(8)}, ${it.latitude().toString().take(8)}"
            }
            return ""
        }
    }
}
