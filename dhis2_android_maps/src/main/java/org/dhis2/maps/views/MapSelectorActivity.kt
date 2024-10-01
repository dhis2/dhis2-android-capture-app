package org.dhis2.maps.views

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.engine.LocationEngineDefault
import com.mapbox.mapboxsdk.location.engine.MapboxFusedLocationEngineImpl
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMoveListener
import org.dhis2.commons.locationprovider.LocationProviderImpl
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.maps.di.Injector
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.getLatLngPointList
import org.dhis2.maps.geometry.polygon.PolygonAdapter
import org.dhis2.maps.location.MapActivityLocationCallback
import org.dhis2.maps.location.MapLocationEngine
import org.dhis2.maps.managers.DefaultMapManager
import org.dhis2.ui.theme.Dhis2Theme
import org.hisp.dhis.android.core.common.FeatureType

class MapSelectorActivity :
    AppCompatActivity(),
    MapActivityLocationCallback.OnLocationChanged {

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initLocationUpdates()
            }
        }

    private val locationProvider = LocationProviderImpl(this)

    override fun onLocationChanged(latLng: LatLng, accuracy: Float) {
        mapSelectorViewModel.onNewLocation(
            SelectedLocation.GPSResult(
                latLng.latitude,
                latLng.longitude,
                accuracy,
            ),
        )
    }

    private val locationListener = LocationListener { location ->
        mapSelectorViewModel.onNewLocation(
            SelectedLocation.GPSResult(
                location.latitude,
                location.longitude,
                location.accuracy,
            ),
        )
    }

    private val locationCallback = MapActivityLocationCallback(this)

    private var fieldUid: String? = null

    var init: Boolean = false

    private lateinit var mapManager: DefaultMapManager

    private val mapSelectorViewModel: MapSelectorViewModel by viewModels<MapSelectorViewModel> {
        Injector.provideMapSelectorViewModelFactory(
            context = this,
            locationType = intent.getStringExtra(LOCATION_TYPE_EXTRA)?.let { featureName ->
                FeatureType.valueOf(featureName)
            } ?: FeatureType.POINT,
            initialCoordinates = intent.getStringExtra(INITIAL_GEOMETRY_COORDINATES),
        )
    }

    private val polygonAdapter = PolygonAdapter(
        onAddPolygonPoint = { mapSelectorViewModel.addPointToPolygon(it) },
        onRemovePolygonPoint = { index, _ -> mapSelectorViewModel.removePointFromPolygon(index) },
    )

    private lateinit var mapboxLocationProvider: MapboxFusedLocationEngineImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fieldUid = intent.getStringExtra(FIELD_UID)

        setContent {
            Dhis2Theme {
                MapSelectorScreen(
                    mapSelectorViewModel = mapSelectorViewModel,
                    mapSelectorScreenActions = MapSelectorScreenActions(
                        onBackClicked = ::finish,
                        onMapDataUpdated = { featureCollection ->
                            mapManager.update(
                                featureCollection = featureCollection,
                                boundingBox = GetBoundingBox().getEnclosingBoundingBox(
                                    featureCollection.features()?.getLatLngPointList()
                                        ?: emptyList(),
                                ),
                            )
                            if (mapSelectorViewModel.shouldDisplayPolygonInfo()) {
                                polygonAdapter.updateWithFeatureCollection(featureCollection)
                            }
                        },
                        onLocationButtonClicked = ::onLocationButtonClicked,
                        loadMap = { loadMap(it, savedInstanceState) },
                        onDoneClicked = { result ->
                            result?.let(::finishResult)
                        },
                        configurePolygonInfoRecycler = {
                            it.adapter = polygonAdapter
                            it.layoutManager = GridLayoutManager(this, 2)
                        },
                    ),
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadMap(mapView: MapView, savedInstanceState: Bundle?) {
        mapManager =
            DefaultMapManager(mapView, MapLocationEngine(this), mapSelectorViewModel.featureType)
        mapManager.also {
            lifecycle.addObserver(it)
            it.onCreate(savedInstanceState)
            it.onMapClickListener = OnMapClickListener(
                mapManager = it,
                onFeatureClicked = { feature ->
                    mapSelectorViewModel.updateSelectedGeometry(feature)
                },
                onPointClicked = { point ->
                    mapSelectorViewModel.onClickedOnMap(point)
                },
            )

            mapboxLocationProvider = MapboxFusedLocationEngineImpl(this)
            mapManager.init(
                mapStyles = mapSelectorViewModel.fetchMapStyles(),
                onInitializationFinished = {
                    with(it.map) {
                        this?.addOnMoveListener(object : OnMoveListener {
                            override fun onMoveBegin(detector: MoveGestureDetector) {
                                // Nothing to do
                            }

                            override fun onMove(detector: MoveGestureDetector) {
                                // TODO: Handle move and uptade if manual selection is enabled
                            }

                            override fun onMoveEnd(detector: MoveGestureDetector) {
                                updateMapVisibleRegion(this@with)
                            }
                        })
                        this?.addOnScaleListener(object : MapboxMap.OnScaleListener {
                            override fun onScaleBegin(detector: StandardScaleGestureDetector) {
                                // Nothing to do
                            }

                            override fun onScale(detector: StandardScaleGestureDetector) {
                                // Nothing to do
                            }

                            override fun onScaleEnd(detector: StandardScaleGestureDetector) {
                                updateMapVisibleRegion(this@with)
                            }
                        })
                        updateMapVisibleRegion(this)
                    }

                    mapSelectorViewModel.init()
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        initLocationUpdates()
                    } else {
                        requestLocationPermission.launch(permission.ACCESS_FINE_LOCATION)
                    }
                },
                onMissingPermission = { permissionsManager ->
                    if (locationProvider.hasLocationEnabled()) {
                        permissionsManager?.requestLocationPermissions(this)
                    } else {
                        LocationSettingLauncher.requestEnableLocationSetting(this)
                    }
                },
            )
        }
    }

    private fun updateMapVisibleRegion(mapboxMap: MapboxMap?) {
        val mapBounds = mapboxMap?.projection?.visibleRegion?.latLngBounds
        mapSelectorViewModel.updateCurrentVisibleRegion(mapBounds)
    }

    private fun onLocationButtonClicked() {
        mapManager.onLocationButtonClicked(
            locationProvider.hasLocationEnabled(),
            this,
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mapManager.permissionsManager?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
        )
    }

    @RequiresPermission(permission.ACCESS_FINE_LOCATION)
    private fun initLocationUpdates() {
        locationProvider.getLastKnownLocation(
            { location -> locationListener.onLocationChanged(location) },
            {},
            {},
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationEngineDefault.getDefaultLocationEngine(this)
            .removeLocationUpdates(locationCallback)
        mapboxLocationProvider.removeLocationUpdates(locationListener)
    }

    private fun finishResult(value: String) {
        val intent = Intent()
        intent.putExtra(FIELD_UID, fieldUid)
        intent.putExtra(DATA_EXTRA, value)
        intent.putExtra(LOCATION_TYPE_EXTRA, mapSelectorViewModel.featureType.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val DATA_EXTRA = "data_extra"
        const val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"
        const val INITIAL_GEOMETRY_COORDINATES = "INITIAL_DATA"
        const val FIELD_UID = "FIELD_UID_EXTRA"

        fun create(
            activity: Context,
            fieldUid: String?,
            locationType: FeatureType,
            initialData: String?,
        ): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            fieldUid.let { intent.putExtra(FIELD_UID, fieldUid) }
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            initialData.let { intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData) }

            return intent
        }
    }
}
