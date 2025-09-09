package org.dhis2.maps.extensions

import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.types.FEATURE_PROPERTY_PLACES
import org.dhis2.maps.layer.types.FEATURE_PROPERTY_PLACES_ID
import org.dhis2.maps.layer.types.FEATURE_PROPERTY_PLACES_SELECTED
import org.dhis2.maps.layer.types.FEATURE_PROPERTY_PLACES_SUBTITLE
import org.dhis2.maps.layer.types.FEATURE_PROPERTY_PLACES_TITLE
import org.maplibre.geojson.Feature
import java.util.UUID

fun Feature.source(): FeatureSource? =
    if (hasProperty(PROPERTY_FEATURE_SOURCE)) {
        FeatureSource.valueOf(getStringProperty(PROPERTY_FEATURE_SOURCE))
    } else {
        null
    }

fun Feature.toStringProperty(): String? =
    when (source()) {
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

fun Feature.withPlacesProperties(
    selected: Boolean = false,
    title: String? = null,
    subtitle: String? = null,
) = this.apply {
    addStringProperty(FEATURE_PROPERTY_PLACES_ID, UUID.randomUUID().toString())
    addBooleanProperty(FEATURE_PROPERTY_PLACES, !selected)
    addBooleanProperty(FEATURE_PROPERTY_PLACES_SELECTED, selected)
    title?.let { addStringProperty(FEATURE_PROPERTY_PLACES_TITLE, it) }
    subtitle?.let { addStringProperty(FEATURE_PROPERTY_PLACES_SUBTITLE, it) }
}
