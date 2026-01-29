package org.dhis2.tracker.search.data

import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntityGeometry
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntitySearchItemProgramOwnerDomain
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemProgramOwner

fun TrackedEntitySearchItem.toTrackedEntitySearchItemResult(): TrackedEntitySearchItemResult =
    TrackedEntitySearchItemResult(
        uid = this.uid,
        created = this.created,
        lastUpdated = this.lastUpdated,
        createdAtClient = this.createdAtClient,
        lastUpdatedAtClient = this.lastUpdatedAtClient,
        organisationUnit = this.organisationUnit,
        geometry = this.geometry.toDomainGeometry(),
        syncState = this.syncState?.toSyncState(),
        aggregatedSyncState = this.aggregatedSyncState?.toSyncState(),
        deleted = this.deleted,
        isOnline = this.isOnline,
        type = this.type.toTrackedEntityType(),
        header = this.header,
        attributeValues = this.attributeValues?.map { it.toTrackedEntitySearchItemAttribute() } ?: emptyList(),
        programOwners = this.programOwners?.map { it.toTrackedEntitySearchItemProgramOwner() } ?: emptyList(),
    )

private fun Geometry?.toDomainGeometry(): TrackedEntityGeometry? =
    this?.type()?.let {
        TrackedEntityGeometry(
            geometryFeatureType = getGeometryType(it),
            coordinates = this.coordinates(),
        )
    }

private fun getGeometryType(type: FeatureType?): GeometryFeatureType =
    when (type) {
        FeatureType.POINT -> GeometryFeatureType.POINT
        FeatureType.MULTI_POLYGON -> GeometryFeatureType.MULTI_POLYGON
        FeatureType.POLYGON -> GeometryFeatureType.POLYGON
        FeatureType.NONE -> GeometryFeatureType.NONE
        FeatureType.SYMBOL -> GeometryFeatureType.SYMBOL
        else -> GeometryFeatureType.NONE
    }

private fun State?.toSyncState(): SyncState? =
    when (this) {
        State.TO_POST -> SyncState.TO_POST
        State.TO_UPDATE -> SyncState.TO_UPDATE
        State.ERROR -> SyncState.ERROR
        State.SYNCED -> SyncState.SYNCED
        State.WARNING -> SyncState.WARNING
        State.UPLOADING -> SyncState.UPLOADING
        State.RELATIONSHIP -> SyncState.RELATIONSHIP
        State.SENT_VIA_SMS -> SyncState.SENT_VIA_SMS
        State.SYNCED_VIA_SMS -> SyncState.SYNCED_VIA_SMS
        null -> null
    }

private fun TrackedEntityType.toTrackedEntityType(): TrackedEntityTypeDomain =
    TrackedEntityTypeDomain(
        trackedEntityTypeAttributeDomains = this.trackedEntityTypeAttributes()?.map { it.toTrackedEntityTypeAttribute() } ?: emptyList(),
        featureType = getGeometryType(this.featureType()),
    )

private fun TrackedEntityTypeAttribute.toTrackedEntityTypeAttribute(): TrackedEntityTypeAttributeDomain =
    TrackedEntityTypeAttributeDomain(
        trackedEntityTypeUid = this.trackedEntityType()?.uid(),
        trackedEntityAttributeUid = this.trackedEntityAttribute()?.uid(),
        displayInList = this.displayInList() ?: false,
        mandatory = this.mandatory() ?: false,
        searchable = this.searchable() ?: false,
        sortOrder = this.sortOrder() ?: 0,
    )

private fun TrackedEntitySearchItemAttribute.toTrackedEntitySearchItemAttribute(): TrackedEntitySearchItemAttributeDomain =
    TrackedEntitySearchItemAttributeDomain(
        attribute = this.attribute,
        displayName = this.displayName,
        displayFormName = this.displayFormName,
        value = this.value,
        created = this.created,
        lastUpdated = this.lastUpdated,
        valueType = this.valueType.toTrackerInputType(),
        displayInList = this.displayInList,
        optionSet = this.optionSet,
    )

private fun ValueType.toTrackerInputType(): TrackerInputType =
    when (this) {
        ValueType.TEXT -> TrackerInputType.TEXT
        ValueType.LONG_TEXT -> TrackerInputType.LONG_TEXT
        ValueType.NUMBER -> TrackerInputType.NUMBER
        ValueType.INTEGER -> TrackerInputType.INTEGER
        ValueType.INTEGER_POSITIVE -> TrackerInputType.INTEGER_POSITIVE
        ValueType.INTEGER_NEGATIVE -> TrackerInputType.INTEGER_NEGATIVE
        ValueType.INTEGER_ZERO_OR_POSITIVE -> TrackerInputType.INTEGER_ZERO_OR_POSITIVE
        ValueType.BOOLEAN -> TrackerInputType.CHECKBOX
        ValueType.TRUE_ONLY -> TrackerInputType.YES_ONLY_CHECKBOX
        ValueType.DATE -> TrackerInputType.DATE
        ValueType.DATETIME -> TrackerInputType.DATE_TIME
        ValueType.TIME -> TrackerInputType.TIME
        ValueType.EMAIL -> TrackerInputType.EMAIL
        ValueType.PHONE_NUMBER -> TrackerInputType.PHONE_NUMBER
        ValueType.PERCENTAGE -> TrackerInputType.PERCENTAGE
        ValueType.URL -> TrackerInputType.URL
        ValueType.COORDINATE -> TrackerInputType.NOT_SUPPORTED
        ValueType.AGE -> TrackerInputType.AGE
        ValueType.ORGANISATION_UNIT -> TrackerInputType.ORGANISATION_UNIT
        ValueType.UNIT_INTERVAL -> TrackerInputType.UNIT_INTERVAL
        ValueType.FILE_RESOURCE -> TrackerInputType.NOT_SUPPORTED
        ValueType.IMAGE -> TrackerInputType.NOT_SUPPORTED
        ValueType.LETTER -> TrackerInputType.LETTER
        ValueType.USERNAME -> TrackerInputType.TEXT
        ValueType.TRACKER_ASSOCIATE -> TrackerInputType.NOT_SUPPORTED
        ValueType.REFERENCE -> TrackerInputType.NOT_SUPPORTED
        ValueType.GEOJSON -> TrackerInputType.NOT_SUPPORTED
        else -> TrackerInputType.TEXT
    }

private fun TrackedEntitySearchItemProgramOwner.toTrackedEntitySearchItemProgramOwner(): TrackedEntitySearchItemProgramOwnerDomain =
    TrackedEntitySearchItemProgramOwnerDomain(
        program = this.program,
        organisationUnit = this.ownerOrgUnit,
    )
