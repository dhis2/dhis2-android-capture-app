package org.dhis2.maps.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.locationprovider.LocationProviderImpl
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.R
import org.dhis2.maps.attribution.AttributionManager
import org.dhis2.maps.camera.initCameraToViewAllElements
import org.dhis2.maps.camera.moveCameraToDevicePosition
import org.dhis2.maps.di.Injector
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.basemaps.BaseMapManager
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.layer.basemaps.BaseMapStyleBuilder.internalBaseMap
import org.dhis2.maps.location.LocationState
import org.dhis2.maps.utils.OnMapReadyIdlingResourceSingleton
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.markerview.MarkerViewManager
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

abstract class MapManager(
    val mapView: MapView,
    private val locationEngine: LocationEngine?,
) : LifecycleEventObserver {
    var map: MapLibreMap? = null
    lateinit var mapLayerManager: MapLayerManager
    private var markerViewManager: MarkerViewManager? = null
    private var symbolManager: SymbolManager? = null
    var onMapClickListener: MapLibreMap.OnMapClickListener? = null
    var style: Style? = null
    var permissionsManager: PermissionsManager? = null
    private var mapStyles: List<BaseMapStyle> = emptyList()

    private val colorUtils: ColorUtils = ColorUtils()
    private val dispatcherProvider = Injector.provideDispatcher()
    private val locationProvider = LocationProviderImpl(mapView.context)

    open var numberOfUiIcons: Int = 2
    open var defaultUiIconLeftMargin = 8.dp
    open var defaultUiIconTopMargin = 9.dp
    open var defaultUiIconRightMargin = 9.dp
    open var defaultUiIconBottomMargin = 0.dp
    open var defaultUiIconSize = 40.dp

    private val _locationState =
        MutableStateFlow(
            when (locationProvider.hasLocationEnabled()) {
                true -> LocationState.NOT_FIXED
                false -> LocationState.OFF
            },
        )
    val locationState = _locationState.asStateFlow()

    private val _dataFinishedLoading = MutableStateFlow(false)
    val dataFinishedLoading = _dataFinishedLoading.asStateFlow()

    fun init(
        mapStyles: List<BaseMapStyle>,
        locationListener: LocationListenerCompat? = null,
        onInitializationFinished: () -> Unit = {},
        onMissingPermission: (PermissionsManager?) -> Unit,
    ) {
        OnMapReadyIdlingResourceSingleton.countingIdlingResource.increment()
        this.mapStyles = mapStyles.ifEmpty { listOf(internalBaseMap()) }
        if (style == null) {
            mapView.getMapAsync { mapLoaded ->
                mapView.contentDescription = "LOADED"
                this.map = mapLoaded
                val baseMapManager = BaseMapManager(mapView.context, this.mapStyles)
                setUi()
                OnMapReadyIdlingResourceSingleton.countingIdlingResource.decrement()
                map?.setStyle(
                    baseMapManager.styleJson(
                        this.mapStyles.find { it.isDefault }
                            ?: mapStyles.first(),
                    ),
                ) { styleLoaded ->
                    this.style = styleLoaded
                    mapLayerManager =
                        MapLayerManager(mapLoaded, baseMapManager, colorUtils).apply {
                            styleChangeCallback = { newStyle ->
                                style = newStyle
                                mapLayerManager.clearLayers()
                                loadDataForStyle()
                                setSource()
                            }
                        }
                    onMapClickListener?.let { mapClickListener ->
                        map?.addOnMapClickListener(mapClickListener)
                    }
                    map?.addOnCameraMoveListener {
                        updateLocationState()
                    }

                    markerViewManager = MarkerViewManager(mapView, map)
                    enableLocationComponent(styleLoaded, onMissingPermission, locationListener)
                    loadDataForStyle()
                    _dataFinishedLoading.value = true
                    CoroutineScope(dispatcherProvider.io()).launch {
                        _dataFinishedLoading.emit(dataFinishedLoading.value)
                    }
                    onInitializationFinished()
                }
            }
        } else {
            onInitializationFinished()
        }
    }

    private fun setUi() {
        map?.apply {
            uiSettings.setAttributionDialogManager(
                AttributionManager(
                    mapView.context,
                    this,
                    mapStyles.first(),
                ),
            )
            uiSettings.isLogoEnabled = false
            uiSettings.setAttributionMargins(
                defaultUiIconLeftMargin,
                uiSettings.attributionMarginTop,
                uiSettings.attributionMarginRight,
                uiSettings.attributionMarginBottom,
            )
            ContextCompat.getDrawable(mapView.context, R.drawable.ic_compass_ripple)?.let {
                uiSettings.setCompassImage(it)
                uiSettings.setCompassMargins(
                    0,
                    numberOfUiIcons * defaultUiIconSize +
                        (numberOfUiIcons + 1) * defaultUiIconTopMargin,
                    defaultUiIconRightMargin,
                    defaultUiIconBottomMargin,
                )
            }
        }
    }

    abstract fun loadDataForStyle()

    abstract fun setSource()

    abstract fun setLayer()

    fun initCameraPosition(boundingBox: BoundingBox) {
        val bounds =
            LatLngBounds
                .Builder()
                .include(pointToLatLn(boundingBox.northeast()))
                .include(pointToLatLn(boundingBox.southwest()))
                .build()
        map?.initCameraToViewAllElements(bounds)
    }

    private fun pointToLatLn(point: Point): LatLng = LatLng(point.latitude(), point.longitude())

    fun onLocationButtonClicked(
        isLocationEnabled: Boolean,
        context: Activity,
    ) {
        if (isLocationEnabled) {
            centerCameraOnMyPosition { permissionManager ->
                permissionManager?.requestLocationPermissions(context)
            }
        } else {
            LocationSettingLauncher.requestEnableLocationSetting(context)
        }
    }

    private fun centerCameraOnMyPosition(onMissingPermission: (PermissionsManager?) -> Unit) {
        val isLocationActivated =
            map?.locationComponent?.isLocationComponentActivated ?: false
        if (isLocationActivated) {
            val isLocationEnabled = map?.locationComponent?.isLocationComponentEnabled ?: false
            if (isLocationEnabled) {
                val location = map?.locationComponent?.lastKnownLocation
                location?.let { map?.moveCameraToDevicePosition(LatLng(location)) }
            }
        } else {
            enableLocationComponentAndCenterCamera(onMissingPermission)
        }
    }

    fun isMapReady() = map != null && style?.isFullyLoaded ?: false

    fun onCreate(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
    }

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }

    abstract fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): Feature?

    open fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): List<Feature>? = emptyList()

    abstract fun findFeature(propertyValue: String): Feature?

    open fun findFeatures(propertyValue: String): List<Feature>? = emptyList()

    open fun getLayerName(source: String): String = source

    open fun markFeatureAsSelected(
        point: LatLng,
        layer: String? = null,
    ): Feature? = null

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(
        style: Style,
        onMissingPermission: (PermissionsManager?) -> Unit,
        locationListener: LocationListenerCompat?,
    ) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    getLocationComponentActivationOptions(style),
                )
                isLocationComponentEnabled = true
                locationProvider.getLastKnownLocation(
                    { location -> locationListener?.onLocationChanged(location) },
                    { onMissingPermission(permissionsManager) },
                    { updateLocationState() },
                )
            } else {
                permissionsManager =
                    PermissionsManager(
                        object : PermissionsListener {
                            override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

                            override fun onPermissionResult(granted: Boolean) {
                                if (granted) {
                                    enableLocationComponent(style, onMissingPermission, locationListener)
                                }
                            }
                        },
                    )
                onMissingPermission(permissionsManager)
            }
        }
    }

    private fun getLocationComponentActivationOptions(style: Style) =
        if (locationEngine != null) {
            LocationComponentActivationOptions
                .builder(mapView.context, style)
                .locationComponentOptions(
                    LocationComponentOptions
                        .builder(mapView.context)
                        .accuracyAnimationEnabled(true)
                        .build(),
                ).useDefaultLocationEngine(false)
                .locationEngine(locationEngine)
                .build()
        } else {
            LocationComponentActivationOptions
                .builder(mapView.context, style)
                .locationComponentOptions(
                    LocationComponentOptions
                        .builder(mapView.context)
                        .accuracyAnimationEnabled(true)
                        .build(),
                ).useDefaultLocationEngine(true)
                .build()
        }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponentAndCenterCamera(onMissingPermission: (PermissionsManager?) -> Unit) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    LocationComponentActivationOptions
                        .builder(
                            mapView.context,
                            style!!,
                        ).build(),
                )
                isLocationComponentEnabled = true
                centerCameraOnMyPosition(onMissingPermission)
            } else {
                permissionsManager =
                    PermissionsManager(
                        object : PermissionsListener {
                            override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

                            override fun onPermissionResult(granted: Boolean) {
                                if (granted) {
                                    centerCameraOnMyPosition(onMissingPermission)
                                }
                            }
                        },
                    )
                onMissingPermission(permissionsManager)
            }
        }
    }

    private fun updateLocationState() {
        map?.apply {
            locationComponent.isLocationComponentActivated
            val currentLocation =
                with(locationComponent) {
                    if (isLocationComponentActivated) {
                        lastKnownLocation?.let { LatLng(it) }
                    } else {
                        null
                    }
                }
            val mapCenter = cameraPosition.target
            val locationState =
                when {
                    !locationProvider.hasLocationEnabled() ||
                        mapCenter == null ||
                        currentLocation == null ->
                        LocationState.OFF

                    locationProvider.hasUpdatesEnabled() &&
                        mapCenter.distanceTo(currentLocation) < (1f) ->
                        LocationState.FIXED

                    else ->
                        LocationState.NOT_FIXED
                }
            CoroutineScope(dispatcherProvider.io()).launch {
                _locationState.emit(locationState)
            }
        }
    }

    fun requestMapLayerManager(): MapLayerManager? =
        if (::mapLayerManager.isInitialized) {
            mapLayerManager
        } else {
            null
        }

    fun updateLayersVisibility(layerVisibility: HashMap<String, Boolean>): HashMap<String, MapLayer> {
        layerVisibility.forEach { (sourceId, visible) ->
            mapLayerManager.handleLayer(sourceId, visible)
        }
        return mapLayerManager.mapLayers
    }

    @SuppressLint("MissingPermission")
    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                mapView.onCreate(null)
            }

            Lifecycle.Event.ON_START -> {
                mapView.onStart()
            }

            Lifecycle.Event.ON_RESUME -> {
                mapView.onResume()
            }

            Lifecycle.Event.ON_PAUSE -> {
                mapView.onPause()
            }

            Lifecycle.Event.ON_STOP -> {
                mapView.onPause()
            }

            Lifecycle.Event.ON_DESTROY -> {
                markerViewManager?.onDestroy()
                symbolManager?.onDestroy()
                if (map?.locationComponent?.isLocationComponentActivated == true) {
                    map?.locationComponent?.isLocationComponentEnabled = false
                }
                if (!mapView.isDestroyed) {
                    mapView.onDestroy()
                }
            }

            Lifecycle.Event.ON_ANY -> {
                // no-op
            }
        }
    }
}
