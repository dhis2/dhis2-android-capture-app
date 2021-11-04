package org.dhis2.android_maps.geometry.mapper

import com.mapbox.geojson.Feature
import org.dhis2.android_maps.model.EventUiComponentModel
import org.dhis2.android_maps.model.RelationshipUiComponentModel
import org.dhis2.commons.data.SearchTeiModel

fun Feature?.addTeiInfo(searchTeiModel: SearchTeiModel): Feature? {
    if (this != null) {
        addStringProperty(org.dhis2.android_maps.extensions.PROPERTY_FEATURE_SOURCE, org.dhis2.android_maps.extensions.FeatureSource.TEI.name)
        addStringProperty(org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_UID, searchTeiModel.tei.uid())
        addStringProperty(org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_IMAGE, searchTeiModel.profilePicturePath)
        if (searchTeiModel.selectedEnrollment != null) {
            addStringProperty(
                org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.ENROLLMENT_UID,
                searchTeiModel.selectedEnrollment.uid()
            )
        }
    }
    return this
}

fun Feature?.addTeiEnrollmentInfo(searchTeiModel: SearchTeiModel): Feature? {
    if (this != null) {
        addStringProperty(org.dhis2.android_maps.extensions.PROPERTY_FEATURE_SOURCE, org.dhis2.android_maps.extensions.FeatureSource.ENROLLMENT.name)
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.ENROLLMENT_UID,
            searchTeiModel.selectedEnrollment.uid()
        )
        addStringProperty(org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_UID, searchTeiModel.tei.uid())
    }
    return this
}

fun Feature?.addRelationshipInfo(
    relationshipUiComponentModel: RelationshipUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(org.dhis2.android_maps.extensions.PROPERTY_FEATURE_SOURCE, org.dhis2.android_maps.extensions.FeatureSource.RELATIONSHIP.name)
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.RELATIONSHIP,
            relationshipUiComponentModel.relationshipTypeUid
        )
        addBooleanProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.BIDIRECTIONAL,
            relationshipUiComponentModel.bidirectional
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.FROM_TEI,
            relationshipUiComponentModel.from.teiUid
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.TO_TEI,
            relationshipUiComponentModel.to.teiUid
        )
    }
    return this
}

fun Feature?.addRelationFromInfo(
    relationshipUiComponentModel: RelationshipUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(org.dhis2.android_maps.extensions.PROPERTY_FEATURE_SOURCE, org.dhis2.android_maps.extensions.FeatureSource.RELATIONSHIP.name)
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_UID,
            relationshipUiComponentModel.from.teiUid
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid
        )
        addNumberProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_IMAGE,
            relationshipUiComponentModel.from.defaultImage
        )
    }
    return this
}

fun Feature?.addRelationToInfo(
    relationshipUiComponentModel: RelationshipUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(org.dhis2.android_maps.extensions.PROPERTY_FEATURE_SOURCE, org.dhis2.android_maps.extensions.FeatureSource.RELATIONSHIP.name)
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_UID,
            relationshipUiComponentModel.to.teiUid
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid
        )
        addNumberProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_IMAGE,
            relationshipUiComponentModel.to.defaultImage
        )
    }
    return this
}

fun Feature?.addTeiEventInfo(
    eventUiComponentModel: EventUiComponentModel
): Feature? {
    if (this != null) {
        addStringProperty(org.dhis2.android_maps.extensions.PROPERTY_FEATURE_SOURCE, org.dhis2.android_maps.extensions.FeatureSource.TRACKER_EVENT.name)
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_UID,
            eventUiComponentModel.enrollment.trackedEntityInstance()
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.TEI_IMAGE,
            eventUiComponentModel.teiImage
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection.EVENT_UID,
            eventUiComponentModel.event.uid()
        )
        addStringProperty(
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection.STAGE_UID,
            eventUiComponentModel.programStage?.uid()
        )
    }
    return this
}
