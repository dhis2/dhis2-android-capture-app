package org.dhis2.maps.views

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonPrimitive
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.camera.initCameraToViewAllElements
import org.dhis2.maps.camera.moveCameraToPosition
import org.dhis2.maps.databinding.ActivityMapSelectorBinding
import org.dhis2.maps.extensions.polygonToLatLngBounds
import org.dhis2.maps.extensions.toPoint
import org.dhis2.maps.geometry.point.PointAdapter
import org.dhis2.maps.geometry.point.PointViewModel
import org.dhis2.maps.geometry.polygon.PolygonAdapter
import org.dhis2.maps.geometry.polygon.PolygonViewModel
import org.dhis2.maps.location.MapActivityLocationCallback
import org.dhis2.maps.utils.updateSource
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

private const val POLYGON_POINT_IMG_ID = "polygon_img"
private const val POLYGON_SOURCE_ID = "polygon_source"
private const val POLYGON_LAYER_ID = "polygon_layer"

class MapSelectorActivity :
    AppCompatActivity(),
    MapActivityLocationCallback.OnLocationChanged {

    override fun onLocationChanged(latLng: Point) {
        if (!init) {
            init = true
            if (initialCoordinates == null) {
                map.moveCameraToPosition(latLng)
            }
        }
    }

    private var fieldUid: String? = null
    lateinit var mapView: MapView
    lateinit var map: MapboxMap
    var style: Style? = null
    private lateinit var locationType: FeatureType
    lateinit var binding: ActivityMapSelectorBinding
    var init: Boolean = false

    private var initialCoordinates: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map_selector)
        binding.back.setOnClickListener { finish() }
        locationType = intent.getStringExtra(LOCATION_TYPE_EXTRA)?.let { featureName ->
            FeatureType.valueOf(featureName)
        } ?: FeatureType.POINT

        fieldUid = intent.getStringExtra(FIELD_UID)
        initialCoordinates = intent.getStringExtra(INITIAL_GEOMETRY_COORDINATES)
        mapView = binding.mapView

        map = mapView.getMapboxMap()
        mapView.contentDescription = "LOADED"
        map.loadStyleUri(Style.MAPBOX_STREETS) { style ->
            this.style = style
            enableLocationComponent()
            centerMapOnCurrentLocation()
            when (locationType) {
                FeatureType.POINT -> bindPoint(initialCoordinates)
                FeatureType.POLYGON -> bindPolygon(initialCoordinates)
                else -> finish()
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationEngineProvider.getBestLocationEngine(this).getLastLocation(
                MapActivityLocationCallback(this)
            )
        }
    }

    private fun bindPoint(initial_coordinates: String?) {
        val pointViewModel = ViewModelProvider(this)[PointViewModel::class.java]
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = PointAdapter(pointViewModel)
        map.addOnMapClickListener {
            val point = Point.fromLngLat(it.longitude().truncate(), it.latitude().truncate())
            setPointToViewModel(point, pointViewModel)
            true
        }
        binding.saveButton.setOnClickListener {
            val value = pointViewModel.getPointAsString()
            value?.let {
                finishResult(it)
            }
        }

        if (initial_coordinates != null) {
            val initGeometry =
                Geometry.builder().coordinates(initial_coordinates).type(locationType).build()
            val pointGeometry = GeometryHelper.getPoint(initGeometry)
            pointGeometry.let { sdkPoint ->
                val point = Point.fromLngLat(sdkPoint[0], sdkPoint[1])
                setPointToViewModel(point, pointViewModel)
            }
            map.moveCameraToPosition(pointGeometry.toPoint())
        }
    }

    private fun setPointToViewModel(point: Point, viewModel: PointViewModel) {
        viewModel.setPoint(point)
        viewModel.source?.let { geoSon ->
            viewModel.source = updateSource(point, geoSon)
            return
        }
        viewModel.source = createSource(viewModel.getId(), point)
        viewModel.layer = createLayer(viewModel.getId())
        showSource(
            viewModel.source!!,
            viewModel.layer!!,
            viewModel.getId(),
            R.drawable.ic_map_item_1
        )
    }

    private fun updateSource(point: Point, source: GeoJsonSource): GeoJsonSource {
        style?.updateSource(source.sourceId, Feature.fromGeometry(point))
        return style?.getSourceAs(source.sourceId)!!
    }

    private fun showSource(source: GeoJsonSource, layer: SymbolLayer, id: String, drawable: Int) {
        style?.addImage(
            id,
            ResourcesCompat.getDrawable(resources, drawable, null)!!.toBitmap()
        )
        style?.addSource(source)
        style?.addLayer(layer)
    }

    private fun createLayer(id: String): SymbolLayer {
        val symbolLayer = SymbolLayer(id, id)
        symbolLayer
            .iconImage(id)
        return symbolLayer
    }

    private fun createSource(id: String, point: Point): GeoJsonSource {
        return GeoJsonSource.Builder(id)
            .feature(
                Feature.fromGeometry(
                    point
                )
            )
            .build()
    }

    private fun bindPolygon(initial_coordinates: String?) {
        style?.addImage(
            POLYGON_POINT_IMG_ID,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_map_item_1, null)!!.toBitmap()
        )
        style?.addLayerBelow(
            FillLayer(POLYGON_LAYER_ID, POLYGON_SOURCE_ID)
                .fillColor("#E71409")
                .fillOpacity(0.3),
            "settlement-label"
        )
        val viewModel = ViewModelProvider(this)[PolygonViewModel::class.java]
        val polygonAdapter = PolygonAdapter(viewModel)
        binding.recycler.adapter = polygonAdapter

        val manager = binding.mapView.annotations.createPointAnnotationManager()
        manager.addDragListener(object : OnPointAnnotationDragListener {
            override fun onAnnotationDrag(annotation: Annotation<*>) {
            }

            override fun onAnnotationDragFinished(annotation: Annotation<*>) {
                val pointAnnotation = (annotation as PointAnnotation)
                viewModel.updatePointPosition(
                    pointAnnotation.point,
                    pointAnnotation.getData()?.asString
                )
            }

            override fun onAnnotationDragStarted(annotation: Annotation<*>) {
            }
        })

        viewModel.onMessage = {
            Toast.makeText(this@MapSelectorActivity, it, Toast.LENGTH_SHORT).show()
        }
        binding.recycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewModel.response.observe(
            this
        ) {
            onPolygonPointsUpdated(it, polygonAdapter, manager)
        }
        map.addOnMapClickListener {
            val point = Point.fromLngLat(it.longitude(), it.latitude())
            val polygonPoint = viewModel.createPolygonPoint()
            polygonPoint.point = point
            polygonPoint.layer = createLayer(polygonPoint.uuid)
            polygonPoint.source = createSource(polygonPoint.uuid, point)
            viewModel.add(polygonPoint)
            true
        }
        binding.saveButton.setOnClickListener {
            val value = viewModel.getPointAsString()
            value?.let {
                finishResult(it)
            }
        }
        if (initial_coordinates != null) {
            val initGeometry =
                Geometry.builder().coordinates(initial_coordinates).type(locationType).build()
            val polygons = GeometryHelper.getPolygon(initGeometry)
            polygons.forEach {
                it.forEachIndexed { index, sdkPoint ->
                    if (index != it.size - 1) {
                        val point = Point.fromLngLat(sdkPoint[0], sdkPoint[1])
                        val polygonPoint = viewModel.createPolygonPoint()
                        polygonPoint.point = point
                        viewModel.add(polygonPoint)
                    }
                }
            }
            polygons.polygonToLatLngBounds()?.let { points ->
                map.initCameraToViewAllElements(this, points)
            }
        }
    }

    private fun onPolygonPointsUpdated(
        polygonPoints: MutableList<PolygonViewModel.PolygonPoint>,
        polygonAdapter: PolygonAdapter,
        dragManager: PointAnnotationManager
    ) {
        dragManager.deleteAll()
        val points = listOf(
            polygonPoints.map { polygonPoint ->
                dragManager.create(
                    PointAnnotationOptions()
                        .withPoint(polygonPoint.point!!)
                        .withIconImage(POLYGON_POINT_IMG_ID)
                        .withData(JsonPrimitive(polygonPoint.uuid))
                        .withDraggable(true)
                )
                polygonPoint.point
            }
        )

        polygonAdapter.updateItems(polygonPoints)
        updateVector(points)
    }

    private fun updateVector(points: List<List<Point?>>) {
        style?.let { style ->
            if (points[0].size > 3) {
                style.updateSource(POLYGON_SOURCE_ID, Polygon.fromLngLats(points))
            }
        }
    }

    private fun centerMapOnCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_LOCATION_PERMISSION_REQUEST
            )
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACCESS_LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    enableLocationComponent()
                    centerMapOnCurrentLocation()
                }
            }
        }
    }

    companion object {
        private const val ACCESS_LOCATION_PERMISSION_REQUEST = 102
        const val DATA_EXTRA = "data_extra"
        const val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"
        const val INITIAL_GEOMETRY_COORDINATES = "INITIAL_DATA"
        const val FIELD_UID = "FIELD_UID_EXTRA"

        fun create(activity: Context, locationType: FeatureType): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            return intent
        }

        fun create(activity: Context, locationType: FeatureType, initialData: String?): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            if (initialData != null) {
                intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
            }
            return intent
        }

        fun create(
            activity: Context,
            fieldUid: String,
            locationType: FeatureType,
            initialData: String?
        ): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(FIELD_UID, fieldUid)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            if (initialData != null) {
                intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
            }
            return intent
        }
    }

    private fun finishResult(value: String) {
        val intent = Intent()
        intent.putExtra(FIELD_UID, fieldUid)
        intent.putExtra(DATA_EXTRA, value)
        intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
        setResult(RESULT_OK, intent)
        finish()
    }
}
