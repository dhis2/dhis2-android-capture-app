package org.dhis2.tracker.search.data

import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntityGeometry
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemProgramOwner

fun transformDomainTeiToSDKTei(trackedEntitySearchItemResult: TrackedEntitySearchItemResult): TrackedEntitySearchItem =
    TrackedEntitySearchItem(
        uid = trackedEntitySearchItemResult.uid,
        created = trackedEntitySearchItemResult.created,
        lastUpdated = trackedEntitySearchItemResult.lastUpdated,
        createdAtClient = trackedEntitySearchItemResult.createdAtClient,
        lastUpdatedAtClient = trackedEntitySearchItemResult.lastUpdatedAtClient,
        organisationUnit = trackedEntitySearchItemResult.organisationUnit,
        geometry = trackedEntitySearchItemResult.geometry?.toSDKGeometry(),
        syncState = trackedEntitySearchItemResult.syncState?.toSDKState(),
        aggregatedSyncState = trackedEntitySearchItemResult.aggregatedSyncState?.toSDKState(),
        deleted = trackedEntitySearchItemResult.deleted,
        isOnline = trackedEntitySearchItemResult.isOnline,
        type = mapTrackedEntityType(trackedEntitySearchItemResult.type),
        header = trackedEntitySearchItemResult.header,
        attributeValues = trackedEntitySearchItemResult.attributeValues.map { it.toSDKAttribute() },
        programOwners =
            trackedEntitySearchItemResult.programOwners.map {
                TrackedEntitySearchItemProgramOwner(
                    program = it.program,
                    ownerOrgUnit = it.organisationUnit,
                )
            },
    )

private fun TrackedEntityGeometry.toSDKGeometry(): org.hisp.dhis.android.core.common.Geometry? =
    coordinates?.let {
        org.hisp.dhis.android.core.common.Geometry
            .builder()
            .type(geometryFeatureType.toSDKFeatureType())
            .coordinates(it)
            .build()
    }

private fun GeometryFeatureType.toSDKFeatureType(): org.hisp.dhis.android.core.common.FeatureType =
    when (this) {
        GeometryFeatureType.POINT -> org.hisp.dhis.android.core.common.FeatureType.POINT
        GeometryFeatureType.POLYGON -> org.hisp.dhis.android.core.common.FeatureType.POLYGON
        GeometryFeatureType.MULTI_POLYGON -> org.hisp.dhis.android.core.common.FeatureType.MULTI_POLYGON
        GeometryFeatureType.NONE -> org.hisp.dhis.android.core.common.FeatureType.NONE
        GeometryFeatureType.SYMBOL -> org.hisp.dhis.android.core.common.FeatureType.POINT
    }

private fun SyncState.toSDKState(): State =
    when (this) {
        SyncState.TO_POST -> State.TO_POST
        SyncState.TO_UPDATE -> State.TO_UPDATE
        SyncState.ERROR -> State.ERROR
        SyncState.SYNCED -> State.SYNCED
        SyncState.WARNING -> State.WARNING
        SyncState.UPLOADING -> State.UPLOADING
        SyncState.RELATIONSHIP -> State.RELATIONSHIP
        SyncState.SENT_VIA_SMS -> State.SENT_VIA_SMS
        SyncState.SYNCED_VIA_SMS -> State.SYNCED_VIA_SMS
    }

private fun TrackedEntitySearchItemAttributeDomain.toSDKAttribute(): TrackedEntitySearchItemAttribute =
    TrackedEntitySearchItemAttribute(
        attribute = attribute,
        displayName = displayName,
        displayFormName = displayFormName,
        value = value,
        created = created,
        lastUpdated = lastUpdated,
        valueType = valueType.toSDKValueType(),
        displayInList = displayInList,
        optionSet = optionSet,
    )

private fun TrackerInputType.toSDKValueType(): ValueType =
    when (this) {
        TrackerInputType.TEXT -> ValueType.TEXT
        TrackerInputType.LONG_TEXT -> ValueType.LONG_TEXT
        TrackerInputType.LETTER -> ValueType.LETTER
        TrackerInputType.PHONE_NUMBER -> ValueType.PHONE_NUMBER
        TrackerInputType.EMAIL -> ValueType.EMAIL
        TrackerInputType.URL -> ValueType.URL
        TrackerInputType.NUMBER -> ValueType.NUMBER
        TrackerInputType.INTEGER -> ValueType.INTEGER
        TrackerInputType.INTEGER_POSITIVE -> ValueType.INTEGER_POSITIVE
        TrackerInputType.INTEGER_NEGATIVE -> ValueType.INTEGER_NEGATIVE
        TrackerInputType.INTEGER_ZERO_OR_POSITIVE -> ValueType.INTEGER_ZERO_OR_POSITIVE
        TrackerInputType.PERCENTAGE -> ValueType.PERCENTAGE
        TrackerInputType.UNIT_INTERVAL -> ValueType.UNIT_INTERVAL
        TrackerInputType.AGE -> ValueType.AGE
        TrackerInputType.ORGANISATION_UNIT -> ValueType.ORGANISATION_UNIT
        TrackerInputType.DATE_TIME -> ValueType.DATETIME
        TrackerInputType.DATE -> ValueType.DATE
        TrackerInputType.TIME -> ValueType.TIME
        TrackerInputType.CHECKBOX,
        TrackerInputType.RADIO_BUTTON,
        TrackerInputType.YES_ONLY_SWITCH,
        TrackerInputType.YES_ONLY_CHECKBOX,
        -> ValueType.BOOLEAN
        TrackerInputType.QR_CODE,
        TrackerInputType.BAR_CODE,
        -> ValueType.TEXT
        TrackerInputType.MULTI_SELECTION -> ValueType.MULTI_TEXT
        TrackerInputType.DROPDOWN,
        TrackerInputType.PERIOD_SELECTOR,
        TrackerInputType.MATRIX,
        TrackerInputType.SEQUENTIAL,
        TrackerInputType.NOT_SUPPORTED,
        TrackerInputType.CUSTOM_INTENT,
        -> ValueType.TEXT
    }

private fun mapTrackedEntityType(trackedEntityTypeDomain: TrackedEntityTypeDomain): TrackedEntityType {
    val firstAttribute = trackedEntityTypeDomain.trackedEntityTypeAttributeDomains.firstOrNull()
    val teiTypeUid = firstAttribute?.trackedEntityTypeUid ?: "unknown"

    return TrackedEntityType
        .builder()
        .uid(teiTypeUid)
        .build()
}
