package org.dhis2.maps

import android.content.Intent
import android.net.Uri
import com.mapbox.geojson.Feature
import org.dhis2.maps.geometry.getPointLatLng
import javax.inject.Inject

open class ExternalMapNavigation @Inject constructor() {
    fun navigateToMapIntent(feature: Feature): Intent {
        val point = feature.getPointLatLng()
        val longitude = point.longitude.toString()
        val latitude = point.latitude.toString()
        val location = "geo:0,0?q=$latitude,$longitude"
        val intent =
            Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(location)
        return intent
    }
}
