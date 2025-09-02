package org.dhis2.maps.geometry.mapper

import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.maplibre.geojson.Feature

internal fun Feature?.addTeiInfo(mapItemModel: MapItemModel): Feature? {
    if (this != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.TEI.name,
        )
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            mapItemModel.uid,
        )
        if (mapItemModel.isProfilePictureAvailable()) {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.profilePicturePath(),
            )
        } else if (mapItemModel.isCustomIcon()) {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.getCustomIconRes(),
            )
        } else {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.getDefaultIconRes(),
            )
        }
        if (mapItemModel.relatedInfo?.enrollment != null) {
            addStringProperty(
                MapTeisToFeatureCollection.ENROLLMENT_UID,
                mapItemModel.relatedInfo.enrollment.uid,
            )
        }
    }
    return this
}

internal fun Feature?.addTeiEnrollmentInfo(mapItemModel: MapItemModel): Feature? {
    if (this != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.ENROLLMENT.name,
        )

        mapItemModel.relatedInfo?.enrollment?.let {
            addStringProperty(
                MapTeisToFeatureCollection.ENROLLMENT_UID,
                mapItemModel.relatedInfo.enrollment.uid,
            )
        }

        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            mapItemModel.uid,
        )
    }
    return this
}

fun Feature?.addRelationshipInfo(relationshipUiComponentModel: RelationshipUiComponentModel): Feature? {
    if (this != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.RELATIONSHIP.name,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP,
            relationshipUiComponentModel.relationshipTypeUid,
        )
        addBooleanProperty(
            MapRelationshipsToFeatureCollection.BIDIRECTIONAL,
            relationshipUiComponentModel.bidirectional,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.FROM_TEI,
            relationshipUiComponentModel.from.teiUid,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.TO_TEI,
            relationshipUiComponentModel.to.teiUid,
        )
    }
    return this
}

fun Feature?.addRelationshipInfo(mapItemModel: MapItemModel): Feature? {
    if (this != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.RELATIONSHIP.name,
        )
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            mapItemModel.uid,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            mapItemModel.relatedInfo?.relationship?.uid,
        )
        if (mapItemModel.isProfilePictureAvailable()) {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.profilePicturePath(),
            )
        } else if (mapItemModel.isCustomIcon()) {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.getCustomIconRes(),
            )
        } else {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.getDefaultIconRes(),
            )
        }
    }
    return this
}

fun Feature?.addRelationFromInfo(relationshipUiComponentModel: RelationshipUiComponentModel): Feature? {
    if (this != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.RELATIONSHIP.name,
        )
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            relationshipUiComponentModel.from.teiUid,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid,
        )
        addNumberProperty(
            MapTeisToFeatureCollection.TEI_IMAGE,
            relationshipUiComponentModel.from.defaultImage,
        )
    }
    return this
}

fun Feature?.addRelationToInfo(relationshipUiComponentModel: RelationshipUiComponentModel): Feature? {
    if (this != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.RELATIONSHIP.name,
        )
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            relationshipUiComponentModel.to.teiUid,
        )
        addStringProperty(
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            relationshipUiComponentModel.relationshipUid,
        )
        addNumberProperty(
            MapTeisToFeatureCollection.TEI_IMAGE,
            relationshipUiComponentModel.to.defaultImage,
        )
    }
    return this
}

internal fun Feature?.addTeiEventInfo(mapItemModel: MapItemModel): Feature? {
    if (this != null && mapItemModel.relatedInfo?.event != null) {
        addStringProperty(
            org.dhis2.maps.extensions.PROPERTY_FEATURE_SOURCE,
            org.dhis2.maps.extensions.FeatureSource.TRACKER_EVENT.name,
        )
        addStringProperty(
            MapTeisToFeatureCollection.TEI_UID,
            mapItemModel.relatedInfo.event.teiUid,
        )
        if (mapItemModel.isProfilePictureAvailable()) {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.profilePicturePath(),
            )
        } else if (mapItemModel.isCustomIcon()) {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.getCustomIconRes(),
            )
        } else {
            addStringProperty(
                MapTeisToFeatureCollection.TEI_IMAGE,
                mapItemModel.getDefaultIconRes(),
            )
        }
        addStringProperty(
            MapTeiEventsToFeatureCollection.EVENT_UID,
            mapItemModel.uid,
        )
        addStringProperty(
            MapTeiEventsToFeatureCollection.STAGE_UID,
            mapItemModel.relatedInfo.event.stageUid,
        )
    }
    return this
}
