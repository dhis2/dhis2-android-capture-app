package org.dhis2.uicomponents.map.geometry.mapper

import com.mapbox.geojson.Feature
import org.dhis2.uicomponents.map.extensions.FeatureSource
import org.dhis2.uicomponents.map.extensions.PROPERTY_FEATURE_SOURCE
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel

fun Feature?.addTeiInfo(searchTeiModel: SearchTeiModel): Feature? {
    if (this != null) {
        addStringProperty(PROPERTY_FEATURE_SOURCE, FeatureSource.TEI.name)
        addStringProperty(MapTeisToFeatureCollection.TEI_UID, searchTeiModel.tei.uid())
        addStringProperty(MapTeisToFeatureCollection.TEI_IMAGE, searchTeiModel.profilePicturePath)
        if (searchTeiModel.selectedEnrollment != null) {
            addStringProperty(
                MapTeisToFeatureCollection.ENROLLMENT_UID,
                searchTeiModel.selectedEnrollment.uid()
            )
        }
    }
    return this
}

fun Feature?.addTeiEnrollmentInfo(searchTeiModel: SearchTeiModel): Feature? {
    if (this != null) {
        addStringProperty(PROPERTY_FEATURE_SOURCE, FeatureSource.ENROLLMENT.name)
        addStringProperty(
            MapTeisToFeatureCollection.ENROLLMENT_UID,
            searchTeiModel.selectedEnrollment.uid()
        )
        addStringProperty(MapTeisToFeatureCollection.TEI_UID, searchTeiModel.tei.uid())
    }
    return this
}

fun Feature?.addRelationshipInfo(
    relationshipUiComponentModel: RelationshipUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(PROPERTY_FEATURE_SOURCE, FeatureSource.RELATIONSHIP.name)
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP,
            relationshipUiComponentModel.relationshipTypeUid
        )
        addBooleanProperty(
            MapRelationshipsToFeatureCollection.BIDIRECTIONAL,
            relationshipUiComponentModel.bidirectional
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.FROM_TEI,
            relationshipUiComponentModel.from.teiUid
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.TO_TEI,
            relationshipUiComponentModel.to.teiUid
        )
    }
    return this
}

fun Feature?.addRelationFromInfo(
    relationshipUiComponentModel: RelationshipUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(PROPERTY_FEATURE_SOURCE, FeatureSource.RELATIONSHIP.name)
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            relationshipUiComponentModel.from.teiUid
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid
        )
        addNumberProperty(
            MapTeisToFeatureCollection.TEI_IMAGE,
            relationshipUiComponentModel.from.defaultImage
        )
    }
    return this
}

fun Feature?.addRelationToInfo(
    relationshipUiComponentModel: RelationshipUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(PROPERTY_FEATURE_SOURCE, FeatureSource.RELATIONSHIP.name)
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            relationshipUiComponentModel.to.teiUid
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid
        )
        addNumberProperty(
            MapTeisToFeatureCollection.TEI_IMAGE,
            relationshipUiComponentModel.to.defaultImage
        )
    }
    return this
}

fun Feature?.addTeiEventInfo(
    eventUiComponentModel: EventUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(PROPERTY_FEATURE_SOURCE, FeatureSource.TRACKER_EVENT.name)
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            eventUiComponentModel.enrollment.trackedEntityInstance()
        )
        addStringProperty(
            MapTeisToFeatureCollection.TEI_IMAGE,
            eventUiComponentModel.teiImage
        )
        addStringProperty(
            MapTeiEventsToFeatureCollection.EVENT_UID,
            eventUiComponentModel.event.uid()
        )
        addStringProperty(
            MapTeiEventsToFeatureCollection.STAGE_UID,
            eventUiComponentModel.programStage?.uid()
        )
    }
    return this
}
