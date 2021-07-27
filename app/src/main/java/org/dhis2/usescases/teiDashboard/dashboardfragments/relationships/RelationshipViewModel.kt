package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipType

data class RelationshipViewModel(
    val relationship: Relationship,
    val fromGeometry: Geometry?,
    val toGeometry: Geometry?,
    val relationshipType: RelationshipType,
    val direction: RelationshipDirection,
    val ownerUid: String,
    val ownerType: RelationshipOwnerType,
    val fromValues: List<Pair<String, String>>,
    val toValues: List<Pair<String, String>>,
    val fromImage: String?,
    val toImage: String?,
    val fromDefaultImageResource: Int,
    val toDefaultImageResource: Int
){
    fun primaryInfo(){

    }
}

enum class RelationshipDirection {
    FROM, TO
}

enum class RelationshipOwnerType {
    EVENT, TEI
}