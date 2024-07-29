package org.dhis2.maps.extensions

import com.mapbox.geojson.Feature
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection

fun Feature.source(): FeatureSource? {
    return if (hasProperty(PROPERTY_FEATURE_SOURCE)) {
        FeatureSource.valueOf(getStringProperty(PROPERTY_FEATURE_SOURCE))
    } else {
        null
    }
}

fun Feature.toStringProperty(): String? = when (source()) {
    FeatureSource.TEI, FeatureSource.ENROLLMENT ->
        getStringProperty(MapTeisToFeatureCollection.TEI_UID)

    FeatureSource.RELATIONSHIP ->
        getStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
        )

    FeatureSource.TRACKER_EVENT ->
        getStringProperty(
            MapTeiEventsToFeatureCollection.EVENT_UID,
        )

    FeatureSource.EVENT ->
        getStringProperty(
            MapEventToFeatureCollection.EVENT,
        )

    FeatureSource.FIELD ->
        getStringProperty(MapTeisToFeatureCollection.TEI_UID)
            ?: getStringProperty(MapTeiEventsToFeatureCollection.EVENT_UID)

    null -> null
}
