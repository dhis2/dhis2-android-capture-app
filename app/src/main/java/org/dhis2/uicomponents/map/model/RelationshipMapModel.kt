package org.dhis2.uicomponents.map.model

import org.hisp.dhis.android.core.common.Geometry

data class RelationshipMapModel(
    val displayName: String?,
    val relationshipUid: String,
    val relationshipTypeUid: String,
    val direction: RelationshipDirection,
    val bidirectional: Boolean?,
    val from: TeiMap,
    val to: TeiMap,
    val canBeDeleted: Boolean? = true
)

data class TeiMap(
    val teiUid: String,
    val geometry: Geometry?,
    val image: String?,
    val defaultIcon: String,
    val mainAttribute: String?
)

enum class RelationshipDirection { FROM, TO }
