package org.dhis2.uicomponents.map.model

import org.hisp.dhis.android.core.common.Geometry

data class RelationshipMapModel(val displayName: String,
                                val relationshipTypeUid: String,
                                val programUid: String,
                                val direction: RelationshipDirection,
                                val bidirectional: Boolean)
data class From(val teiUid: String, val imagePath:String, val geometry: Geometry)
data class To(val teiUid: String, val imagePath:String, val geometry: Geometry)
enum class RelationshipDirection{ FROM, TO }
