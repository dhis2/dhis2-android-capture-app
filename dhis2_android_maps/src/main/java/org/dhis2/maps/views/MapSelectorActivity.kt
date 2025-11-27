package org.dhis2.maps.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.location.LocationListenerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.maps.camera.MapSelectorZoomHandler
import org.dhis2.maps.di.Injector
import org.dhis2.maps.geometry.polygon.PolygonAdapter
import org.dhis2.maps.location.MapLocationEngine
import org.dhis2.maps.managers.DefaultMapManager
import org.dhis2.maps.model.MapScope
import org.dhis2.maps.model.MapSelectorScreenActions
import org.dhis2.maps.model.MapSelectorScreenState
import org.dhis2.maps.utils.GeometryCoordinate
import org.dhis2.maps.utils.addMoveListeners
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.maplibre.android.maps.MapView

class MapSelectorActivity : AppCompatActivity() {
    private val locationProvider = MapLocationEngine(this)

    private val locationListener =
        LocationListenerCompat { location ->
            mapSelectorViewModel.onNewLocation(
                SelectedLocation.GPSResult(
                    location.latitude,
                    location.longitude,
                    location.accuracy,
                ),
            )
        }

    private var fieldUid: String? = null

    var init: Boolean = false

    private lateinit var mapManager: DefaultMapManager

    private val mapSelectorViewModel: MapSelectorViewModel by viewModels<MapSelectorViewModel> {
        Injector.provideMapSelectorViewModelFactory(
            context = this,
            locationType =
                intent.getStringExtra(LOCATION_TYPE_EXTRA)?.let { featureName ->
                    FeatureType.valueOf(featureName)
                } ?: FeatureType.POINT,
            initialCoordinates = intent.getStringExtra(INITIAL_GEOMETRY_COORDINATES),
            uid = intent.getStringExtra(PROGRAM_UID),
            scope =
                intent.getStringExtra(SCOPE)?.let { scope ->
                    MapScope.valueOf(scope)
                } ?: MapScope.PROGRAM,
        )
    }

    private val polygonAdapter =
        PolygonAdapter(
            onAddPolygonPoint = { mapSelectorViewModel.addPointToPolygon(it) },
            onRemovePolygonPoint = { index, _ -> mapSelectorViewModel.removePointFromPolygon(index) },
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fieldUid = intent.getStringExtra(FIELD_UID)

        setContent {
            DHIS2Theme {
                val screenState by mapSelectorViewModel.screenState.collectAsState()

                ObserveAsEvents(mapSelectorViewModel.geometryCoordinateResultChannel) { geometryCoordinates ->
                    geometryCoordinates?.let(::finishResult)
                }

                MapSelectorScreen(
                    screenState = screenState,
                    mapSelectorScreenActions =
                        MapSelectorScreenActions(
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
                                mapSelectorViewModel.onMyLocationButtonClick()
                                onLocationButtonClicked()
                            },
                            onDoneButtonClick = mapSelectorViewModel::onDoneClick,
                        ),
                )

                LaunchedEffect(screenState.mapData) {
                    mapManager.update(
                        featureCollection = screenState.mapData.featureCollection,
                        boundingBox = screenState.mapData.boundingBox,
                    )
                    initZoom(screenState)

                    if (screenState.displayPolygonInfo) {
                        polygonAdapter.updateWithFeatureCollection(screenState.mapData)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    private fun loadMap(
        mapView: MapView,
        savedInstanceState: Bundle?,
    ) {
        mapManager =
            DefaultMapManager(mapView, locationProvider, mapSelectorViewModel.featureType)
        mapManager.also {
            lifecycle.addObserver(it)
            it.onCreate(savedInstanceState)
            it.onMapClickListener =
                OnMapClickListener(
                    mapManager = it,
                    onFeatureClicked = mapSelectorViewModel::onPinClicked,
                    onPointClicked = mapSelectorViewModel::onPointClicked,
                )

            val mapData = mapSelectorViewModel.screenState.value.mapData
            mapManager.init(
                mapStyles = mapSelectorViewModel.fetchMapStyles(),
                onInitializationFinished = {
                    mapManager.update(
                        featureCollection = mapData.featureCollection,
                        boundingBox = mapData.boundingBox,
                    )
                    initZoom(mapSelectorViewModel.screenState.value)
                    it.map?.addMoveListeners(
                        onIdle = { bounds ->
                            mapSelectorViewModel.updateCurrentVisibleRegion(bounds)
                            mapSelectorViewModel.onMoveEnd()
                        },
                        onMove = mapSelectorViewModel::onMove,
                    )
                    lifecycleScope.launch {
                        mapManager.locationState.collect { locationState ->
                            mapSelectorViewModel.updateLocationState(locationState)
                        }
                    }
                },
                onMissingPermission = { permissionsManager ->
                    if (locationProvider.hasLocationEnabled()) {
                        permissionsManager?.requestLocationPermissions(this)
                    } else {
                        LocationSettingLauncher.requestEnableLocationSetting(this)
                    }
                },
                locationListener = locationListener,
            )
        }
    }

    private fun initZoom(screenState: MapSelectorScreenState) {
        MapSelectorZoomHandler(
            mapManager.map,
            screenState.captureMode,
            screenState.mapData.featureCollection,
            screenState.lastGPSLocation,
        )
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

    override fun onDestroy() {
        super.onDestroy()
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
        const val PROGRAM_UID = "PROGRAM_UID_EXTRA"
        const val SCOPE = "SCOPE"

        fun create(
            activity: Context,
            fieldUid: String?,
            locationType: FeatureType,
            initialData: String?,
            programUid: String?,
        ): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            fieldUid.let { intent.putExtra(FIELD_UID, fieldUid) }
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            initialData.let { intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData) }
            initialData.let { intent.putExtra(PROGRAM_UID, programUid) }

            return intent
        }
    }
}
