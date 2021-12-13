package org.dhis2.maps.managers

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import org.dhis2.commons.bindings.dp
import org.dhis2.maps.R
import org.dhis2.maps.camera.initCameraToViewAllElements
import org.dhis2.maps.camera.moveCameraToDevicePosition
import org.dhis2.maps.carousel.CarouselAdapter
import org.dhis2.maps.layer.MapLayerManager

abstract class MapManager(val mapView: MapView) : LifecycleObserver {

    var map: MapboxMap? = null
    lateinit var mapLayerManager: MapLayerManager
    var markerViewManager: MarkerViewManager? = null
    var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null
    var carouselAdapter: CarouselAdapter? = null
    var style: Style? = null
    var permissionsManager: PermissionsManager? = null

    fun init(
        onInitializationFinished: () -> Unit = {},
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        if (style == null) {
            mapView.getMapAsync { mapLoaded ->
                mapView.contentDescription = "LOADED"
                this.map = mapLoaded
                setUi()
                map?.setStyle(Style.MAPBOX_STREETS) { styleLoaded ->
                    this.style = styleLoaded
                    mapLayerManager = MapLayerManager(mapLoaded).apply {
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
            ContextCompat.getDrawable(mapView.context, R.drawable.ic_compass_ripple)?.let {
                uiSettings.setCompassImage(it)
                uiSettings.setCompassMargins(8.dp, 56.dp, 7.dp, 0.dp)
            }
        }
    }

    abstract fun loadDataForStyle()
    abstract fun setSource()
    abstract fun setLayer()

    fun initCameraPosition(
        boundingBox: BoundingBox
    ) {
        val bounds = LatLngBounds.Builder()
            .include(pointToLatLn(boundingBox.northeast()))
            .include(pointToLatLn(boundingBox.southwest()))
            .build()
        map?.initCameraToViewAllElements(
            mapView.context, bounds
        )
    }

    fun pointToLatLn(point: Point): LatLng {
        return LatLng(point.latitude(), point.longitude())
    }

    fun centerCameraOnMyPosition(onMissingPermission: (PermissionsManager?) -> Unit) {
        val isLocationActivated =
            map?.locationComponent?.let { it.isLocationComponentActivated } ?: false
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

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        mapView.onStart()
        map?.locationComponent?.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        mapView.onResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        mapView.onPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        mapView.onStop()
    }

    @SuppressLint("MissingPermission")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mapView.onDestroy()
        markerViewManager?.onDestroy()
        symbolManager?.onDestroy()
        map?.locationComponent?.onStop()
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }

    abstract fun findFeature(source: String, propertyName: String, propertyValue: String): Feature?
    open fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String
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
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        mapView.context,
                        style
                    ).build()
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
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        mapView.context,
                        style!!
                    ).build()
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
}
