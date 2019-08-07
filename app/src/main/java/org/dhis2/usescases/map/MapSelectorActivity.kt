package org.dhis2.usescases.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder.createSource
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.Source
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.databinding.ActivityMapSelectorBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.map.multipolygon.MultiPolygonViewModel
import org.dhis2.usescases.map.point.PointAdapter
import org.dhis2.usescases.map.point.PointViewModel
import org.dhis2.usescases.map.polygon.PolygonAdapter
import org.dhis2.usescases.map.polygon.PolygonViewModel
import org.hisp.dhis.android.core.common.FeatureType
import java.util.*


/**
 * Created by Cristian on 15/03/2018.
 */

class MapSelectorActivity : ActivityGlobalAbstract() {

    lateinit var mapView: MapView
    lateinit var map: MapboxMap
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var style: Style? = null
    lateinit var location_type: FeatureType
    lateinit var binding: ActivityMapSelectorBinding
    val arrayOfIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map_selector)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.back.setOnClickListener { v -> finish() }
        location_type = FeatureType.valueOf(intent.getStringExtra(LOCATION_TYPE_EXTRA))
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            map = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                this.style = style
                centerMapOnCurrentLocation()
            }
            when (location_type) {
                FeatureType.MULTI_POLYGON -> bindMultiPolygon()
                FeatureType.POINT -> bindPolygon()
                FeatureType.POLYGON -> bindPolygon()
                else -> finish()
            }
        }
    }


    private fun bindPoint() {
        val viewModel =  ViewModelProviders.of(this).get(PointViewModel::class.java)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = PointAdapter(viewModel)
        map.addOnMapClickListener { it ->
            val point = Point.fromLngLat(it.longitude, it.latitude)
            setPointToViewModel(point, viewModel)
            true
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

    private fun bindPolygon() {
        val viewModel =  ViewModelProviders.of(this).get(PolygonViewModel::class.java)
        binding.recycler.layoutManager = GridLayoutManager(this, 2)
        viewModel.response.observe(this, Observer<MutableList<PolygonViewModel.PolygonPoint>> {
            binding.recycler.adapter = PolygonAdapter(it, viewModel)
            updateVector(it)
        })
        map.addOnMapClickListener { it ->
            val point = Point.fromLngLat(it.longitude, it.latitude)
            val polygonPoint = viewModel.createPolygonPoint()
            polygonPoint.point = point
            polygonPoint.layer = createLayer(polygonPoint.uuid)
            polygonPoint.source = createSource(polygonPoint.uuid, point)
            viewModel.add(polygonPoint)
            true
        }
    }

    private fun bindMultiPolygon() {
        val viewModel =  ViewModelProviders.of(this).get(MultiPolygonViewModel::class.java)

    }

    private fun updateVector(list: MutableList<PolygonViewModel.PolygonPoint>) {
        style?.let { style ->
            val sourceName = "polygon_source"
            style.removeLayer(sourceName)
            style.removeSource(sourceName)
            arrayOfIds.forEach {
                style.getLayer(it)?.let{layer ->
                    style.removeLayer(layer)
                }
                style.getSource(it)?.let {
                    style.removeSource(it)
                }
            }
            arrayOfIds.clear()
            val points = mutableListOf<MutableList<Point>>()
            points.add(mutableListOf())
            list.forEach {point ->
                point.point?.let {
                    points[0].add(it)
                    arrayOfIds.add(point.uuid)
                    printPoint(it, point.source!!, point.layer!!, point.uuid, R.drawable.ic_oval_green)
                }
            }
            if (points[0].size > 3) {
                if (style.getSource(sourceName) == null) {
                    style.addSource(GeoJsonSource(sourceName, Polygon.fromLngLats(points)))
                    style.addLayerBelow(FillLayer(sourceName, sourceName).withProperties(
                            fillColor(resources.getColor(R.color.green_transparent))), "settlement-label"
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
        mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13.0))

                val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
                        .zoom(15.0)                   // Sets the zoom
                        .build()                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
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
        val LATITUDE = "latitude"
        val POLYGON_DATA = "polygon_data"
        val MULTI_POLYGON_DATA = "multi_polygon_data"
        val LONGITUDE = "longitude"
        private val points = ArrayList<CustomMark>()
        private val index = 0
        private val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"

        fun create(activity: Activity, locationType: FeatureType): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            return intent
        }
    }
}

internal class CustomMark(var marker: MutableList<MutableList<Point>>, var uuid: String)

