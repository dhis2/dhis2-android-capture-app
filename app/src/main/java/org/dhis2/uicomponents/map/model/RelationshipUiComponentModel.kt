package org.dhis2.uicomponents.map.model

import org.hisp.dhis.android.core.common.Geometry

data class RelationshipUiComponentModel(
    val displayName: String?,
    val relationshipTypeUid: String,
    val direction: RelationshipDirection,
    val bidirectional: Boolean?,
    val from: TeiMap,
    val To: TeiMap
)

data class TeiMap(
    val teiUid: String?,
    val geometry: Geometry?,
    val image: String?,
    val defaultImage: String?
)

enum class RelationshipDirection { FROM, TO }
