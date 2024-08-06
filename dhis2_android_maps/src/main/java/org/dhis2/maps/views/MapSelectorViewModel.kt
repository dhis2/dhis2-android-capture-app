package org.dhis2.maps.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.extensions.toLatLng
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.toAccuracyRance
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import com.mapbox.geojson.Geometry as MapGeometry

class MapSelectorViewModel(
    private val featureType: FeatureType,
    private val initialCoordinates: String?,
    private val mapStyleConfig: MapStyleConfiguration,
) : ViewModel() {

    private var currentUserLocation: LatLng? = null
    private var currentUserLocationAccuracy: Float? = null

    private var currentFeature: Feature? = null
    private val _featureCollection = MutableSharedFlow<FeatureCollection>()
    val featureCollection: Flow<FeatureCollection> = _featureCollection
    private val _accuracyRange = MutableSharedFlow<AccuracyRange>()
    val accuracyRange: Flow<AccuracyRange> = _accuracyRange

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val geometry: MapGeometry? = initialCoordinates?.let {
                val geometry = Geometry.builder()
                    .type(featureType)
                    .coordinates(initialCoordinates)
                    .build()
                when (featureType) {
                    FeatureType.POINT -> buildPointGeometry(geometry)
                    FeatureType.POLYGON -> buildPolygonGeometry(geometry)
                    else ->
                        null
                }
            }
            geometry?.let { updateCurrentGeometry(it) }
        }
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }

    private suspend fun updateCurrentGeometry(geometry: MapGeometry) {
        Feature.fromGeometry(geometry)?.let {
            _featureCollection.emit(
                FeatureCollection.fromFeature(it),
            )
            currentFeature = it
        }
    }

    private fun buildPointGeometry(geometry: Geometry): MapGeometry {
        return with(GeometryHelper.getPoint(geometry).toLatLng()) {
            Point.fromLngLat(longitude, latitude)
        }
    }

    private fun buildPolygonGeometry(geometry: Geometry): MapGeometry {
        return with(GeometryHelper.getPolygon(geometry)) {
            val polygonPointList = map { polygon ->
                polygon.map {
                    Point.fromLngLat(it[0], it[1])
                }
            }
            Polygon.fromLngLats(polygonPointList)
        }
    }

    fun updateSelectedGeometry(point: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val newPoint = Point.fromLngLat(point.longitude, point.latitude)
            when (featureType) {
                FeatureType.POINT -> updateCurrentGeometry(newPoint)

                FeatureType.POLYGON -> {
                    val coordinates = (currentFeature?.geometry() as Polygon?)?.coordinates()
                        ?.map { points ->
                            val newList = points.toMutableList()
                            newList.add(newPoint)
                            newList
                        } ?: listOf(listOf(newPoint))
                    updateCurrentGeometry(
                        Polygon.fromLngLats(coordinates),
                    )
                }

                else -> {
                    // no-op
                }
            }
        }
    }

    private fun onNewLocationAccuracy(accuracy: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _accuracyRange.emit(accuracy.toAccuracyRance())
        }
    }

    fun onSaveCurrentGeometry(onValueReady: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = async {
                when (val geometry = currentFeature?.geometry()) {
                    is Point -> {
                        Gson().toJson(
                            geometry.coordinates().map { coordinate -> coordinate.truncate() },
                        )
                    }

                    is Polygon -> {
                        val value = geometry.coordinates().map { polygon ->
                            polygon.map { point ->
                                point.coordinates().map { coordinate -> coordinate.truncate() }
                            }
                        }
                        Gson().toJson(value)
                    }

                    else -> null
                }
            }
            onValueReady(result.await())
        }
    }

    fun onNewLocation(latLng: LatLng, accuracy: Float) {
        if (currentUserLocation == null || accuracy < currentUserLocationAccuracy!!) {
            currentUserLocation = latLng
            currentUserLocationAccuracy = accuracy
            onNewLocationAccuracy(accuracy)
        }
        when {
            featureType == FeatureType.POINT && initialCoordinates == null ->
                updateSelectedGeometry(latLng)
        }
    }

    fun addPointToPolygon(polygonPoint: List<Double>) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch {
                currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .add(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))
                    updateCurrentGeometry(geometry)
                } ?: updateCurrentGeometry(
                    Polygon.fromLngLats(
                        listOf(listOf(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))),
                    ),
                )
            }
        }
    }

    fun removePointFromPolygon(index: Int) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch {
                currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .removeAt(index)
                    updateCurrentGeometry(geometry)
                }
            }
        }
    }
}
