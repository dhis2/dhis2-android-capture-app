package org.dhis2.usescases.map.polygon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class PolygonViewModel(app: Application): AndroidViewModel(app) {

    private var _response = MutableLiveData<List<PolygonPoint>>()
    val response: LiveData<List<PolygonPoint>>
        get() = _response




    inner class PolygonPoint(val point: Point?, val source: GeoJsonSource?, val layer: SymbolLayer?, val selected: Boolean = true)
}