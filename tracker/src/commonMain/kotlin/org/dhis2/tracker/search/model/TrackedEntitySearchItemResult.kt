package org.dhis2.tracker.search.model

import org.dhis2.tracker.ui.input.model.TrackerInputType
import java.util.Date

data class TrackedEntitySearchItemResult(
    val uid: String,
    val created: Date?,
    val lastUpdated: Date?,
    val createdAtClient: Date?,
    val lastUpdatedAtClient: Date?,
    val organisationUnit: String?,
    val geometry: TrackedEntityGeometry?,
    val syncState: SyncState?,
    val aggregatedSyncState: SyncState?,
    val deleted: Boolean,
    val isOnline: Boolean = false,
    val type: TrackedEntityTypeDomain,
    val header: String? = null,
    val attributeValues: List<TrackedEntitySearchItemAttributeDomain>,
    val programOwners: List<TrackedEntitySearchItemProgramOwnerDomain>,
)

data class TrackedEntitySearchItemAttributeDomain(
    val attribute: String,
    val displayName: String,
    val displayFormName: String,
    val value: String?,
    val created: Date?,
    val lastUpdated: Date?,
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

data class TrackedEntitySearchItemProgramOwnerDomain(
    val program: String,
    val organisationUnit: String,
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
