package org.dhis2.common.idlingresources

import androidx.test.espresso.IdlingResource
import androidx.test.rule.ActivityTestRule
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import org.dhis2.R

class MapIdlingResource(
    activityTestRule: ActivityTestRule<*>
) : IdlingResource, OnMapReadyCallback {

    var mapboxMap: MapboxMap? = null
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    init {
        try {
            val mapView = activityTestRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync(this)
        } catch (err: Exception) {
            throw RuntimeException(err)
        }
    }

    override fun getName(): String = javaClass.simpleName

    override fun isIdleNow() = mapboxMap != null

    override fun registerIdleTransitionCallback(resourceCallback: IdlingResource.ResourceCallback) {
        this.resourceCallback = resourceCallback
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        if (resourceCallback != null) {
            resourceCallback!!.onTransitionToIdle()
        }
    }

    fun getMap() = mapboxMap
}