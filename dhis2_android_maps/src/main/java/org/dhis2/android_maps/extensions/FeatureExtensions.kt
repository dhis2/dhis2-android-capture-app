package org.dhis2.android_maps.extensions

import com.mapbox.geojson.Feature

fun Feature.source(): FeatureSource? {
    return if (hasProperty(PROPERTY_FEATURE_SOURCE)) {
        FeatureSource.valueOf(getStringProperty(PROPERTY_FEATURE_SOURCE))
    } else {
        null
    }
}
