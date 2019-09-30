package org.dhis2.usescases.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.databinding.ActivityMapSelectorBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.map.multipolygon.MultiPolygonViewModel
import org.dhis2.usescases.map.point.PointAdapter
import org.dhis2.usescases.map.point.PointViewModel
import org.dhis2.usescases.map.polygon.PolygonAdapter
import org.dhis2.usescases.map.polygon.PolygonViewModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import timber.log.Timber


/**
 * Created by Cristian on 15/03/2018.
 */

class MapSelectorActivity : ActivityGlobalAbstract(), MapActivityLocationCallback.OnLocationChanged {

    override fun onLocationChanged(latLng: LatLng) {
        Timber.d("NEW LOCATION %s, %s", latLng.latitude, latLng.longitude)

        if (!init) {
            init = true
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latLng.latitude, latLng.longitude), 13.0))

            val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(latLng.latitude, latLng.longitude))      // Sets the center of the map to location user
                    .zoom(15.0)                   // Sets the zoom
                    .build()                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    lateinit var mapView: MapView
    lateinit var map: MapboxMap
    var style: Style? = null
    lateinit var location_type: FeatureType
    lateinit var binding: ActivityMapSelectorBinding
    val arrayOfIds = mutableListOf<String>()
    var init: Boolean = false

    var initial_coordinates: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map_selector)
        binding.back.setOnClickListener { v -> finish() }
        location_type = FeatureType.valueOf(intent.getStringExtra(LOCATION_TYPE_EXTRA))
        initial_coordinates = intent.getStringExtra(INITIAL_GEOMETRY_COORDINATES)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            map = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                this.style = style
                enableLocationComponent()
                centerMapOnCurrentLocation()
                when (location_type) {
                    FeatureType.MULTI_POLYGON -> bindMultiPolygon(initial_coordinates)
                    FeatureType.POINT -> bindPoint(initial_coordinates)
                    FeatureType.POLYGON -> bindPolygon(initial_coordinates)
                    else -> finish()
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent() {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            val locationComponent = map?.locationComponent

            // Activate with a built LocationComponentActivationOptions object
            locationComponent?.activateLocationComponent(LocationComponentActivationOptions.builder(this, style!!).build())

            // Enable to make component visible
            locationComponent?.isLocationComponentEnabled = true

            // Set the component's camera mode
            locationComponent?.cameraMode = CameraMode.TRACKING

            // Set the component's render mode
            locationComponent?.renderMode = RenderMode.COMPASS

            locationComponent.zoomWhileTracking(13.0)

            LocationEngineProvider.getBestLocationEngine(this).getLastLocation(
                    MapActivityLocationCallback(this)
            )

        } else {

            /*  permissionsManager = PermissionsManager(this)

              permissionsManager?.requestLocationPermissions(this)*/

        }
    }


    private fun bindPoint(initial_coordinates: String?) {
        val viewModel = ViewModelProviders.of(this).get(PointViewModel::class.java)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = PointAdapter(viewModel)
        map.addOnMapClickListener {
            val point = Point.fromLngLat(it.longitude, it.latitude)
            setPointToViewModel(point, viewModel)
            true
        }
        binding.saveButton.setOnClickListener {
            val value = viewModel.getPointAsString()
            value?.let {
                finishResult(it)
            }
        }

        if (initial_coordinates != null) {
            val initGeometry = Geometry.builder().coordinates(initial_coordinates).type(location_type).build()
            GeometryHelper.getPoint(initGeometry).let { sdkPoint ->
                val point = Point.fromLngLat(sdkPoint[0], sdkPoint[1])
                setPointToViewModel(point, viewModel)
            }
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
        showSource(viewModel.source!!, viewModel.layer!!, viewModel.getId(), R.drawable.mapbox_marker_icon_default)
    }

    private fun updateSource(point: Point, source: GeoJsonSource): GeoJsonSource {
        val geoJson = (style?.getSource(source.id) as GeoJsonSource)
        geoJson.setGeoJson(
                Feature.fromGeometry(
                        point)
        )
        return geoJson
    }

    private fun showSource(source: GeoJsonSource, layer: SymbolLayer, id: String, drawable: Int) {
        style?.addImage(id,
                BitmapFactory.decodeResource(
                        this.resources, drawable))
        style?.addSource(source)
        style?.addLayer(layer)
    }

    private fun printPoint(point: Point, source: GeoJsonSource, layer: SymbolLayer, id: String, drawable: Int) {
        if (style?.getSource(source.id) != null) {
            updateSource(point, source)
        } else {
            showSource(source, layer, id, drawable)
        }
    }

    private fun createLayer(id: String): SymbolLayer {
        val symbolLayer = SymbolLayer(id, id)
        symbolLayer.withProperties(
                PropertyFactory.iconImage(id)
        )
        return symbolLayer
    }

    private fun createSource(id: String, point: Point): GeoJsonSource {
        val geoJsonSource = GeoJsonSource(id, Feature.fromGeometry(
                point))
        return geoJsonSource
    }

    private fun bindPolygon(initial_coordinates: String?) {
        val viewModel = ViewModelProviders.of(this).get(PolygonViewModel::class.java)
        binding.recycler.layoutManager = GridLayoutManager(this, 2)
        viewModel.response.observe(this, Observer<MutableList<PolygonViewModel.PolygonPoint>> {
            binding.recycler.adapter = PolygonAdapter(it, viewModel)
            updateVector(it)
        })
        map.addOnMapClickListener {
            val point = Point.fromLngLat(it.longitude, it.latitude)
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
            val initGeometry = Geometry.builder().coordinates(initial_coordinates).type(location_type).build()
            GeometryHelper.getPolygon(initGeometry).forEach {
                it.forEach { sdkPoint ->
                    val point = Point.fromLngLat(sdkPoint[0], sdkPoint[1])
                    val polygonPoint = viewModel.createPolygonPoint()
                    polygonPoint.point = point
                    polygonPoint.layer = createLayer(polygonPoint.uuid)
                    polygonPoint.source = createSource(polygonPoint.uuid, point)
                    viewModel.add(polygonPoint)
                }
            }
        }
    }

    private fun bindMultiPolygon(initial_coordinates: String?) {
        val viewModel = ViewModelProviders.of(this).get(MultiPolygonViewModel::class.java)

    }

    private fun updateVector(list: MutableList<PolygonViewModel.PolygonPoint>) {
        style?.let { style ->
            val sourceName = "polygon_source"
            style.removeLayer(sourceName)
            style.removeSource(sourceName)
            arrayOfIds.forEach {
                style.getLayer(it)?.let { layer ->
                    style.removeLayer(layer)
                }
                style.getSource(it)?.let {
                    style.removeSource(it)
                }
            }
            arrayOfIds.clear()
            val points = mutableListOf<MutableList<Point>>()
            points.add(mutableListOf())
            list.forEach { point ->
                point.point?.let {
                    points[0].add(it)
                    arrayOfIds.add(point.uuid)
                    printPoint(it, point.source!!, point.layer!!, point.uuid, R.drawable.ic_oval_green)
                }
            }
            if (points[0].size > 2) {
                if (style.getSource(sourceName) == null) {
                    style.addSource(GeoJsonSource(sourceName, Polygon.fromLngLats(points)))
                    style.addLayerBelow(FillLayer(sourceName, sourceName).withProperties(
                            fillColor(resources.getColor(R.color.green_7ed))), "settlement-label"
                    )
                } else {
                    (style.getSource(sourceName) as GeoJsonSource).setGeoJson(Polygon.fromLngLats(points))
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun centerMapOnCurrentLocation() {
        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // TODO CRIS
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }*/
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), ACCESS_COARSE_LOCATION_PERMISSION_REQUEST)
            return
        }

        /* mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
             if (location != null) {
                 map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13.0))

                 val cameraPosition = CameraPosition.Builder()
                         .target(LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
                         .zoom(15.0)                   // Sets the zoom
                         .build()                   // Creates a CameraPosition from the builder
                 map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
             }
         }*/
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACCESS_COARSE_LOCATION_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    centerMapOnCurrentLocation()
                } else {
                    // TODO CRIS
                }
            }
        }
    }

    companion object {
        private val ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 102
        val DATA_EXTRA = "data_extra"
        val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"
        val INITIAL_GEOMETRY_COORDINATES = "INITIAL_DATA"

        fun create(activity: Activity, locationType: FeatureType): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            return intent
        }

        fun create(activity: Activity, locationType: FeatureType, initialData: String?): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            if (initialData != null)
                intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
            return intent
        }
    }

    private fun finishResult(value: String) {
        val intent = Intent()
        intent.putExtra(DATA_EXTRA, value)
        intent.putExtra(LOCATION_TYPE_EXTRA, location_type.toString())
        setResult(RESULT_OK, intent)
        finish()
    }
}


