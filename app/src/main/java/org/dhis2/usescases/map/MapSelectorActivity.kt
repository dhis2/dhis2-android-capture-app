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
import androidx.lifecycle.ViewModelProviders
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
import org.dhis2.usescases.map.polygon.PolygonViewModel
import org.hisp.dhis.android.core.period.FeatureType
import java.util.*


/**
 * Created by Cristian on 15/03/2018.
 */

class MapSelectorActivity : ActivityGlobalAbstract() {

    lateinit var mapView: MapView
    lateinit var map: MapboxMap
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var style: Style
    private val markers = ArrayList<Source>()
    private val layers = ArrayList<Layer>()
    lateinit var location_type: FeatureType
    lateinit var binding: ActivityMapSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map_selector)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.back.setOnClickListener { v -> finish() }
        location_type = FeatureType.valueOf(intent.getStringExtra(LOCATION_TYPE_EXTRA))
            /*
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (points.size() <= index) {
                        points.add(new CustomMark(new ArrayList<>(), UUID.randomUUID().toString()));
                        points.get(index).marker.add(new ArrayList<>());
                    }
                    Point point = Point.fromLngLat(map.getCameraPosition().target.getLongitude(),
                            map.getCameraPosition().target.getLatitude());
                    points.get(index).marker.get(0).add(point);
                    if (location_type == FeatureType.POINT) {
                        if (map != null && map.getCameraPosition().target != null) {
                            setList();
                        } else {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                        return;
                    }
                    addPoint(point);

                }
            });

    */

        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (points.size() <= index) {
                    points.add(new CustomMark(new ArrayList<>(), UUID.randomUUID().toString()));
                    points.get(index).marker.add(new ArrayList<>());
                }
                Point point = Point.fromLngLat(map.getCameraPosition().target.getLongitude(),
                        map.getCameraPosition().target.getLatitude());
                points.get(index).marker.get(0).add(point);
                if (location_type == FeatureType.POINT) {
                    if (map != null && map.getCameraPosition().target != null) {
                        setList();
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                    return;
                }
                addPoint(point);

            }
        });

*/
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
                FeatureType.POINT -> bindPoint()
                FeatureType.POLYGON -> bindPolygon()
                else -> finish()
            }
        }
    }

    private fun setList() {
        val data = Intent()
        when (location_type) {
            FeatureType.POLYGON -> {
                val returnList = ArrayList<Point>()
                for (lst in points) {
                    returnList.addAll(lst.marker[0])
                }
                data.putExtra(LATITUDE, returnList[0].latitude().toString())
                data.putExtra(LONGITUDE, returnList[0].longitude().toString())
                data.putExtra(POLYGON_DATA, Gson().toJson(returnList))
            }
            FeatureType.POINT -> {
                data.putExtra(LATITUDE, map.cameraPosition.target.latitude.toString())
                data.putExtra(LONGITUDE, map.cameraPosition.target.longitude.toString())
            }
            FeatureType.MULTI_POLYGON -> {
                val returnList = ArrayList<List<Point>>()
                for (lst in points) {
                    returnList.addAll(lst.marker)
                }
                data.putExtra(LATITUDE, returnList[0][0].latitude().toString())
                data.putExtra(LONGITUDE, returnList[0][0].longitude().toString())
                data.putExtra(MULTI_POLYGON_DATA, Gson().toJson(returnList))
            }
            else -> finish()
        }

        setResult(Activity.RESULT_OK, data)
        finish()
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
            val geoJson = (style.getSource(geoSon.id) as GeoJsonSource)
            geoJson.setGeoJson(
                    Feature.fromGeometry(
                            point)
            )
            viewModel.source = geoJson
            return
        }
        style.addImage(viewModel.getId(),
                BitmapFactory.decodeResource(
                        this.resources, R.drawable.mapbox_marker_icon_default))
        viewModel.source = createSource(viewModel.getId(), point)
        viewModel.layer = createLayer(viewModel.getId())
    }

    private fun createLayer(id: String): SymbolLayer {
        val symbolLayer = SymbolLayer(id, id)
        symbolLayer.withProperties(
                PropertyFactory.iconImage(id)
        )
        style.addLayer(symbolLayer)
        return symbolLayer
    }

    private fun createSource(id: String, point: Point): GeoJsonSource {
        val geoJsonSource = GeoJsonSource(id, Feature.fromGeometry(
                point))
        style.addSource(geoJsonSource)
        return geoJsonSource
    }

    private fun bindPolygon() {
        val viewModel =  ViewModelProviders.of(this).get(PolygonViewModel::class.java)

    }

    private fun bindMultiPolygon() {
        val viewModel =  ViewModelProviders.of(this).get(MultiPolygonViewModel::class.java)

    }


    fun updateVector() {
        for (s in markers) {
            style.removeSource(s)
        }
        for (l in layers) {
            style.removeLayer(l)
        }
        markers.clear()
        layers.clear()
        for (lst in points) {
            val list = ArrayList<List<Point>>()
            for (pointsList in lst.marker) {
                if (pointsList.size == 1) {
                    pointsList.add(pointsList[0])
                }
                if (pointsList[0].longitude() != pointsList[pointsList.size - 1].longitude() && pointsList[0].latitude() != pointsList[pointsList.size - 1].latitude()) {
                    pointsList.add(pointsList[0])
                }
                list.add(pointsList)
            }
            if (style.getSource(lst.uuid) == null) {
                style.addSource(GeoJsonSource(lst.uuid, Polygon.fromLngLats(list)))
                val rnd = Random()
                val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                style.addLayer(FillLayer(lst.uuid, lst.uuid).withProperties(
                        fillColor(color))
                )
            } else {
                (style.getSource(lst.uuid) as GeoJsonSource).setGeoJson(Polygon.fromLngLats(list))
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

