package org.dhis2.uicomponents.map.model

import org.hisp.dhis.android.core.common.Geometry

data class RelationshipMapModel(val displayName: String?,
                                val relationshipTypeUid: String,
                                val direction: RelationshipDirection,
                                val bidirectional: Boolean?,
                                val from: TeiMap,
                                val To: TeiMap)
data class TeiMap(val teiUid: String?, val geometry: Geometry?)
enum class RelationshipDirection{ FROM, TO }
