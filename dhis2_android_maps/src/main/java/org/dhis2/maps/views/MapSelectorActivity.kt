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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.engine.LocationEngineDefault
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.locationprovider.LocationProviderImpl
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.maps.di.Injector
import org.dhis2.maps.geometry.polygon.PolygonAdapter
import org.dhis2.maps.location.MapActivityLocationCallback
import org.dhis2.maps.location.MapLocationEngine
import org.dhis2.maps.managers.DefaultMapManager
import org.dhis2.maps.model.MapSelectorScreenActions
import org.dhis2.maps.utils.GeometryCoordinate
import org.dhis2.maps.utils.addMoveListeners
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fieldUid = intent.getStringExtra(FIELD_UID)

        setContent {
            Dhis2Theme {
                val screenState by mapSelectorViewModel.screenState.collectAsState()

                ObserveAsEvents(mapSelectorViewModel.geometryCoordinateResultChannel) { geometryCoordinates ->
                    geometryCoordinates?.let(::finishResult)
                }

                MapSelectorScreen(
                    screenState = screenState,
                    mapSelectorScreenActions = MapSelectorScreenActions(
                        onBackClicked = ::finish,
                        loadMap = { loadMap(it, savedInstanceState) },
                        configurePolygonInfoRecycler = {
                            it.adapter = polygonAdapter
                            it.layoutManager = GridLayoutManager(this, 2)
                        },
                        onClearLocation = mapSelectorViewModel::onClearSearchClicked,
                        onSearchLocation = mapSelectorViewModel::onSearchLocation,
                        onLocationSelected = mapSelectorViewModel::onLocationSelected,
                        onSearchCaptureMode = mapSelectorViewModel::initSearchMode,
                        onButtonMode = {
                            if (::mapManager.isInitialized) {
                                mapManager.updateCameraPosition()
                            }
                        },
                        onSearchOnAreaClick = mapSelectorViewModel::onSearchOnAreaClick,
                        onMyLocationButtonClick = {
                            mapSelectorViewModel::onMyLocationButtonClick
                            onLocationButtonClicked()
                        },
                        onDoneButtonClick = mapSelectorViewModel::onDoneClick,
                    ),
                )

                LaunchedEffect(screenState.mapData) {
                    this.launch {
                        delay(500)
                        mapManager.update(
                            featureCollection = screenState.mapData.featureCollection,
                            boundingBox = screenState.mapData.boundingBox,
                        )
                        if (screenState.displayPolygonInfo) {
                            polygonAdapter.updateWithFeatureCollection(screenState.mapData)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(flow, lifecycleOwner.lifecycle) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.Main.immediate) {
                    flow.collect(onEvent)
                }
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
                onFeatureClicked = mapSelectorViewModel::onPinClicked,
                onPointClicked = mapSelectorViewModel::onMapClicked,
            )

            mapManager.init(
                mapStyles = mapSelectorViewModel.fetchMapStyles(),
                onInitializationFinished = {
                    it.map?.addMoveListeners(
                        onIdle = {
                            mapSelectorViewModel.updateCurrentVisibleRegion(it)
                            mapSelectorViewModel.onMoveEnd()
                        },
                        onMove = mapSelectorViewModel::onMove,
                    )

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
        locationProvider.stopLocationUpdates()
    }

    private fun finishResult(value: GeometryCoordinate) {
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
