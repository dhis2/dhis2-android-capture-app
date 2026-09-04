package org.dhis2.tracker.search.model

import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.relationships.model.RelationshipModel
import kotlin.time.Instant

data class TrackedEntitySearchItemResult(
    val uid: String,
    val created: Instant?,
    val lastUpdated: Instant?,
    val createdAtClient: Instant?,
    val lastUpdatedAtClient: Instant?,
    val ownerOrgUnit: String?,
    val enrollmentOrgUnit: String?,
    val shouldDisplayOrgUnit: Boolean,
    val geometry: TrackedEntityGeometry?,
    val syncState: SyncState?,
    val aggregatedSyncState: SyncState?,
    val deleted: Boolean,
    val isOnline: Boolean,
    val teTypeName: String?,
    val type: TrackedEntityTypeDomain,
    val header: String?,
    val overDueDate: Instant?,
    val selectedEnrollment: DomainEnrollment?,
    val profilePicture: String?,
    val enrolledPrograms: List<DomainProgram>?,
    val enrollments: List<DomainEnrollment>?,
    val relationships: List<RelationshipModel>?,
    val defaultTypeIcon: String?,
    val attributeValues: List<TrackedEntitySearchItemAttributeDomain>,
)

class DomainEnrollment(
    val uid: String,
    val orgUnit: String?,
    val program: String?,
    val enrollmentDate: Instant?,
    val incidentDate: Instant?,
    val completedDate: Instant?,
    val followUp: Boolean,
    val status: EnrollmentStatus,
    val trackedEntityInstance: String?,
)

class DomainProgram(
    val uid: String,
    val displayName: String,
    val style: DomainObjectStyle,
)

class DomainObjectStyle(
    val icon: String?,
    val color: String?,
)

enum class EnrollmentStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
}

data class TrackedEntitySearchItemAttributeDomain(
    val attribute: String,
    val displayName: String,
    val displayFormName: String,
    val value: String?,
    val valueType: TrackerInputType,
    val displayInList: Boolean,
    val optionSet: String?,
)

data class TrackedEntityTypeDomain(
    val trackedEntityTypeAttributeDomains: List<TrackedEntityTypeAttributeDomain>,
    val featureType: GeometryFeatureType,
)

data class TrackedEntityTypeAttributeDomain(
    val trackedEntityTypeUid: String?,
    val trackedEntityAttributeUid: String?,
    val displayInList: Boolean,
    val mandatory: Boolean,
    val searchable: Boolean,
    val sortOrder: Int,
)

data class TrackedEntityGeometry(
    val geometryFeatureType: GeometryFeatureType,
    val coordinates: String?,
)

enum class GeometryFeatureType(
    val featureType: String,
    val geometryType: String,
) {
    POINT("POINT", "Point"),
    POLYGON("POLYGON", "Polygon"),
    MULTI_POLYGON("MULTI_POLYGON", "MultiPolygon"),
    NONE("NONE", "None"),
    SYMBOL("SYMBOL", "Symbol"),
}

enum class SyncState {
    TO_POST,
    TO_UPDATE,
    ERROR,
    SYNCED,
    WARNING,
    UPLOADING,
    RELATIONSHIP,
    SENT_VIA_SMS,
    SYNCED_VIA_SMS,
}
