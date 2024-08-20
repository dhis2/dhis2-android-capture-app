package org.dhis2.maps.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.engine.LocationEngine
import com.mapbox.mapboxsdk.location.permissions.PermissionsListener
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.R
import org.dhis2.maps.attribution.AttributionManager
import org.dhis2.maps.camera.initCameraToViewAllElements
import org.dhis2.maps.camera.moveCameraToDevicePosition
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.basemaps.BaseMapManager
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.layer.basemaps.BaseMapStyleBuilder.internalBaseMap
import org.dhis2.maps.utils.OnMapReadyIdlingResourceSingleton

abstract class MapManager(
    val mapView: MapView,
    val locationEngine: LocationEngine?,
) : LifecycleEventObserver {

    var map: MapboxMap? = null
    lateinit var mapLayerManager: MapLayerManager
    private var markerViewManager: MarkerViewManager? = null
    private var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null
    var style: Style? = null
    var permissionsManager: PermissionsManager? = null
    private var mapStyles: List<BaseMapStyle> = emptyList()

    private val colorUtils: ColorUtils = ColorUtils()

    open var numberOfUiIcons: Int = 2
    private val defaultUiIconLeftMargin = 8.dp
    private val defaultUiIconTopMargin = 9.dp
    private val defaultUiIconRightMargin = 9.dp
    private val defaultUiIconBottomMargin = 0.dp
    private val defaultUiIconSize = 40.dp

    fun init(
        mapStyles: List<BaseMapStyle>,
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
                    mapLayerManager = MapLayerManager(mapLoaded, baseMapManager, colorUtils).apply {
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
                    markerViewManager = MarkerViewManager(mapView, map)
                    loadDataForStyle()
                    enableLocationComponent(styleLoaded, onMissingPermission)
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
        val bounds = LatLngBounds.Builder()
            .include(pointToLatLn(boundingBox.northeast()))
            .include(pointToLatLn(boundingBox.southwest()))
            .build()
        map?.initCameraToViewAllElements(bounds)
    }

    private fun pointToLatLn(point: Point): LatLng {
        return LatLng(point.latitude(), point.longitude())
    }

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

    abstract fun findFeature(source: String, propertyName: String, propertyValue: String): Feature?
    open fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): List<Feature>? {
        return emptyList()
    }

    abstract fun findFeature(propertyValue: String): Feature?
    open fun findFeatures(propertyValue: String): List<Feature>? {
        return emptyList()
    }

    open fun getLayerName(source: String): String {
        return source
    }

    open fun markFeatureAsSelected(point: LatLng, layer: String? = null): Feature? {
        return null
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(
        style: Style,
        onMissingPermission: (PermissionsManager?) -> Unit,
    ) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    LocationComponentActivationOptions
                        .builder(mapView.context, style)
                        .locationComponentOptions(
                            LocationComponentOptions.builder(mapView.context)
                                .accuracyAnimationEnabled(true)
                                .build(),
                        ).apply {
                            if (this@MapManager.locationEngine != null) {
                                useDefaultLocationEngine(false)
                                locationEngine(this@MapManager.locationEngine)
                            } else {
                                useDefaultLocationEngine(true)
                            }
                        }
                        .build(),
                )
                isLocationComponentEnabled = true
            } else {
                permissionsManager = PermissionsManager(object : PermissionsListener {
                    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

                    override fun onPermissionResult(granted: Boolean) {
                        if (granted) {
                            enableLocationComponent(style, onMissingPermission)
                        }
                    }
                })
                onMissingPermission(permissionsManager)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponentAndCenterCamera(
        onMissingPermission: (PermissionsManager?) -> Unit,
    ) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        mapView.context,
                        style!!,
                    ).build(),
                )
                isLocationComponentEnabled = true
                centerCameraOnMyPosition(onMissingPermission)
            } else {
                permissionsManager = PermissionsManager(object : PermissionsListener {
                    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

                    override fun onPermissionResult(granted: Boolean) {
                        if (granted) {
                            centerCameraOnMyPosition(onMissingPermission)
                        }
                    }
                })
                onMissingPermission(permissionsManager)
            }
        }
    }

    fun requestMapLayerManager(): MapLayerManager? {
        return if (::mapLayerManager.isInitialized) {
            mapLayerManager
        } else {
            null
        }
    }

    fun updateLayersVisibility(layerVisibility: HashMap<String, Boolean>): HashMap<String, MapLayer> {
        layerVisibility.forEach { (sourceId, visible) ->
            mapLayerManager.handleLayer(sourceId, visible)
        }
        return mapLayerManager.mapLayers
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
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
                mapView.onDestroy()
            }

            Lifecycle.Event.ON_ANY -> {
                // no-op
            }
        }
    }
}
