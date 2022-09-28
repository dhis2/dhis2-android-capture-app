package org.dhis2.maps.managers

import android.annotation.SuppressLint
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import org.dhis2.commons.bindings.dp
import org.dhis2.maps.R
import org.dhis2.maps.camera.initCameraToViewAllElements
import org.dhis2.maps.camera.moveCameraToDevicePosition
import org.dhis2.maps.carousel.CarouselAdapter
import org.dhis2.maps.layer.MapLayerManager

abstract class MapManager(val mapView: MapView) : LifecycleObserver {

    var map: MapboxMap? = null
    lateinit var mapLayerManager: MapLayerManager

    var onMapClickListener: OnMapClickListener? = null
    var carouselAdapter: CarouselAdapter? = null
    var style: Style? = null
    var permissionsManager: PermissionsManager? = null

    var numberOfUiIcons: Int = 2
    val defaultUiIconLeftMargin = 8.dp
    val defaultUiIconTopMargin = 9.dp
    val defaultUiIconRightMargin = 9.dp
    val defaultUiIconBottomMargin = 0.dp
    val defaultUiIconSize = 40.dp

    fun init(
        onInitializationFinished: () -> Unit = {},
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        if (style == null) {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                this.map = mapView.getMapboxMap()
                this.map?.loadStyleUri(Style.MAPBOX_STREETS) { styleLoaded ->
                    setUi()
                    this.style = styleLoaded
                    mapLayerManager = MapLayerManager(this.map!!).apply {
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
                    loadDataForStyle()
                    enableLocationComponent()
                    onInitializationFinished()
                }
            } else {
                permissionsManager = PermissionsManager(object : PermissionsListener {
                    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

                    override fun onPermissionResult(granted: Boolean) {
                        if (granted) {
                            init(onInitializationFinished, onMissingPermission)
                        }
                    }
                })
                onMissingPermission(permissionsManager)
            }
        } else {
            onInitializationFinished()
        }
    }

    private fun setUi() {
        mapView.scalebar.updateSettings {
            this.enabled = false
        }
        mapView.logo.updateSettings {
            this.position = Gravity.TOP or Gravity.START
        }
        mapView.attribution.updateSettings {
            this.position = Gravity.TOP or Gravity.START
        }
        mapView.compass.updateSettings {
            ContextCompat.getDrawable(mapView.context, R.drawable.ic_compass_ripple)?.let {
                image = it
                marginLeft = defaultUiIconLeftMargin.toFloat()
                marginTop = (
                    numberOfUiIcons * defaultUiIconSize +
                        (numberOfUiIcons + 1) * defaultUiIconTopMargin
                    ).toFloat()
                marginRight = defaultUiIconRightMargin.toFloat()
                marginBottom = defaultUiIconBottomMargin.toFloat()
            }
        }
    }

    abstract fun loadDataForStyle()
    abstract fun setSource()
    abstract fun setLayer()

    fun initCameraPosition(
        boundingBox: BoundingBox
    ) {
        map?.apply {
            cameraForCoordinates(listOf(boundingBox.northeast(), boundingBox.southwest()))
        }

        map?.initCameraToViewAllElements(
            mapView.context, listOf(boundingBox.northeast(), boundingBox.southwest())
        )
    }

    @SuppressLint("MissingPermission")
    fun centerCameraOnMyPosition(onMissingPermission: (PermissionsManager?) -> Unit) {
        val isLocationActivated = mapView.location.enabled

        if (isLocationActivated) {
            LocationEngineProvider.getBestLocationEngine(mapView.context)
                .getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
                    override fun onSuccess(result: LocationEngineResult?) {
                        result?.lastLocation?.let {
                            map?.moveCameraToDevicePosition(
                                Point.fromLngLat(
                                    it.longitude,
                                    it.latitude
                                )
                            )
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        TODO("Not yet implemented")
                    }
                })
        } else {
            enableLocationComponentAndCenterCamera(onMissingPermission)
        }
    }

    fun isMapReady() = map != null && style?.isStyleLoaded ?: false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        mapView.onStart()
        mapView.location.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        mapView.onStop()
    }

    @SuppressLint("MissingPermission")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mapView.onDestroy()
        mapView.location.onStop()
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

    open fun markFeatureAsSelected(
        point: Point,
        layer: String? = null,
        onFeature: (Feature?) -> Unit
    ) {
    }

    private fun enableLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.mapbox_user_icon
                ),
                shadowImage = AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.mapbox_user_icon_shadow
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponentAndCenterCamera(
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        enableLocationComponent()
        centerCameraOnMyPosition(onMissingPermission)
    }

    fun requestMapLayerManager(): MapLayerManager? {
        return if (::mapLayerManager.isInitialized) {
            mapLayerManager
        } else {
            null
        }
    }
}
