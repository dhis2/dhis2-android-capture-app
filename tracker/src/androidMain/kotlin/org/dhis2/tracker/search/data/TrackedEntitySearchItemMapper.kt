package org.dhis2.tracker.search.data

import org.dhis2.mobile.commons.extensions.toKtxInstant
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.EnrollmentStatus
import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntityGeometry
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemAttribute
import kotlin.time.Instant
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus as SDKEnrollmentStatus

fun TrackedEntitySearchItem.toTrackedEntitySearchItemResult(
    selectedEnrollment: DomainEnrollment?,
    isOnline: Boolean,
    overDueDate: Instant?,
    ownerOrgUnit: String?,
    enrollmentOrgUnit: String?,
    shouldDisplayOrgUnit: Boolean,
    profilePicture: String?,
    enrollments: List<DomainEnrollment>?,
    enrolledPrograms: List<DomainProgram>?,
    relationships: List<RelationshipModel>?,
): TrackedEntitySearchItemResult =
    TrackedEntitySearchItemResult(
        uid = this.uid,
        created = this.created?.toKtxInstant(),
        lastUpdated = this.lastUpdated?.toKtxInstant(),
        createdAtClient = this.createdAtClient?.toKtxInstant(),
        lastUpdatedAtClient = this.lastUpdatedAtClient?.toKtxInstant(),
        ownerOrgUnit = ownerOrgUnit,
        enrollmentOrgUnit = enrollmentOrgUnit,
        shouldDisplayOrgUnit = shouldDisplayOrgUnit,
        geometry = this.geometry.toDomainGeometry(),
        syncState = this.syncState?.toSyncState(),
        aggregatedSyncState = this.aggregatedSyncState?.toSyncState(),
        deleted = this.deleted,
        isOnline = isOnline,
        teTypeName = this.type.displayName(),
        type = this.type.toTrackedEntityType(),
        header = this.header,
        overDueDate = overDueDate,
        selectedEnrollment = selectedEnrollment,
        profilePicture = profilePicture,
        enrolledPrograms = enrolledPrograms,
        defaultTypeIcon = this.type.style()?.icon(),
        enrollments = enrollments,
        relationships = relationships,
        attributeValues = this.attributeValues?.map { it.toTrackedEntitySearchItemAttribute() } ?: emptyList(),
    )

fun Enrollment.toDomainEnrollment(): DomainEnrollment =
    DomainEnrollment(
        uid = this.uid(),
        orgUnit = this.organisationUnit(),
        program = this.program(),
        enrollmentDate = this.enrollmentDate()?.toKtxInstant(),
        incidentDate = this.incidentDate()?.toKtxInstant(),
        completedDate = this.completedDate()?.toKtxInstant(),
        followUp = this.followUp() ?: false,
        status =
            when (this.status()) {
                SDKEnrollmentStatus.ACTIVE -> EnrollmentStatus.ACTIVE
                SDKEnrollmentStatus.COMPLETED -> EnrollmentStatus.COMPLETED
                SDKEnrollmentStatus.CANCELLED -> EnrollmentStatus.CANCELLED
                null -> EnrollmentStatus.ACTIVE
            },
        trackedEntityInstance = this.trackedEntityInstance(),
    )

private fun Geometry?.toDomainGeometry(): TrackedEntityGeometry? =
    this?.let {
        TrackedEntityGeometry(
            geometryFeatureType = getGeometryType(it.type()),
            coordinates = it.coordinates(),
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
        ValueType.BOOLEAN -> TrackerInputType.HORIZONTAL_RADIOBUTTONS
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
        ValueType.MULTI_TEXT -> TrackerInputType.MULTI_SELECTION
    }

fun SyncState.toSDKState(): State =
    when (this) {
        SyncState.SYNCED -> State.SYNCED
        SyncState.TO_POST -> State.TO_POST
        SyncState.TO_UPDATE -> State.TO_UPDATE
        SyncState.ERROR -> State.ERROR
        SyncState.WARNING -> State.WARNING
        SyncState.UPLOADING -> State.UPLOADING
        SyncState.RELATIONSHIP -> State.RELATIONSHIP
        SyncState.SENT_VIA_SMS -> State.SENT_VIA_SMS
        SyncState.SYNCED_VIA_SMS -> State.SYNCED_VIA_SMS
    }
